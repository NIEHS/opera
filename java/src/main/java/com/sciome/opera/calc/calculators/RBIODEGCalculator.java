package com.sciome.opera.calc.calculators;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.sciome.opera.calc.math.CalcRegParam;
import com.sciome.opera.calc.math.ClassicalLeverage;
import com.sciome.opera.calc.math.KnnPred;
import com.sciome.opera.calc.math.Utils;
import com.sciome.opera.descriptors.OPERAChemicalDescriptors;
import com.sciome.opera.enums.DistanceType;
import com.sciome.opera.enums.Models;
import com.sciome.opera.enums.PretreatmentType;
import com.sciome.opera.model.ClassicalLeverageResult;
import com.sciome.opera.model.Prediction;
import com.sciome.opera.model.Res;
import com.sciome.opera.model.results.DefaultModelResult;
import com.sciome.opera.model.results.OperaModelResult;
import com.sciome.opera.model.training.model.RBIODEG.RBIODEGSet;
import com.sciome.opera.model.training.model.RBIODEG.RBIODEGTraining;

public class RBIODEGCalculator extends ModelCalculator {
	RBIODEGTraining rbiodegTraining;
	
	public RBIODEGCalculator()
	{
		this.rbiodegTraining = RBIODEGTraining.getInstance();
		this.model = Models.RBIODEG;
	}
	
	@Override
	public List<OperaModelResult> calc(List<OPERAChemicalDescriptors> descriptors, boolean neighbors, boolean exp) {
		List<OperaModelResult> resultList = new ArrayList<>();
		
		double[][] XtestArray = new double[descriptors.size()][rbiodegTraining.getDesc().size()];
		for (int i = 0; i < XtestArray.length; i++) {
			for (int j = 0; j < XtestArray[i].length; j++) {
				XtestArray[i][j] = Double.valueOf(descriptors.get(i).getPadelValue(rbiodegTraining.getDesc().get(j).toLowerCase()));
			}
		}
		RealMatrix Xtest = MatrixUtils.createRealMatrix(XtestArray);
		
		RBIODEGSet set = rbiodegTraining.getModel().getSet();
		Prediction pred = KnnPred.predict(Xtest, Utils.matrixFromList(set.getTrain()), Utils.vectorFromIntegerList(set.getClazz()), set.getK(),
				DistanceType.findByName(set.getDist_type()),set.getParam().getPret_type());
		
		ClassicalLeverageResult AD = ClassicalLeverage.classicalLeverage(Utils.matrixFromList(set.getTrain()), Xtest, PretreatmentType.auto);
		
		RealVector AD_Model = Utils.absVector(AD.getInorout().mapSubtractToSelf(1.0));
		
		//Add back in in 2.4
//		for(int i = 0; i < pred.getDc().getRowDimension(); i++) {
//			if(pred.getDc().getEntry(i, 0) == 0)
//				AD_Model.setEntry(i, 1);
//		}
		
		RealVector AD_index = Utils.zeros(Xtest.getRowDimension());
		for(int i = 0; i < pred.getDc().getRowDimension(); i++) {
			AD_index.setEntry(i, 1 / (1 + Utils.medianOfVector(pred.getDc().getRowVector(i))));
		}
		RealVector Conf_index = Utils.zeros(Xtest.getRowDimension());
		
		List<List<String>> CAS_neighbor = new ArrayList<>();
		List<List<String>> InChiKey_neighbor = new ArrayList<>();
		List<List<String>> DTXSID_neighbor = new ArrayList<>();
		List<List<String>> DSSTOXMPID_neighbor = new ArrayList<>();
		List<List<String>> Exp_neighbor = new ArrayList<>();
		List<List<String>> pred_neighbor = new ArrayList<>();
		
		for(int i = 0; i < descriptors.size(); i++) {
			List<String> CAS_neighbor_inner = new ArrayList<>();
			List<String> InChiKey_neighbor_inner = new ArrayList<>();
			List<String> DTXSID_neighbor_inner = new ArrayList<>();
			List<String> DSSTOXMPID_neighbor_inner = new ArrayList<>();
			List<String> Exp_neighbor_inner = new ArrayList<>();
			List<String> pred_neighbor_inner = new ArrayList<>();
			for(int j = 0; j < pred.getNeighbors().getColumnDimension(); j++) {
				CAS_neighbor_inner.add(rbiodegTraining.getCas().get((int)pred.getNeighbors().getEntry(i, j) - 1));
				InChiKey_neighbor_inner.add(rbiodegTraining.getInChiKey().get((int)pred.getNeighbors().getEntry(i, j) - 1));
				DTXSID_neighbor_inner.add(rbiodegTraining.getDtxSid().get((int)pred.getNeighbors().getEntry(i, j) - 1));
				DSSTOXMPID_neighbor_inner.add(rbiodegTraining.getDssToxMpId().get((int)pred.getNeighbors().getEntry(i, j) - 1));
				Exp_neighbor_inner.add("" + (set.getClazz().get((int)pred.getNeighbors().getEntry(i, j) - 1) - 1));
				pred_neighbor_inner.add("" + (rbiodegTraining.getModel().getClass_calc().get((int)pred.getNeighbors().getEntry(i, j) - 1) - 1));
			}
			CAS_neighbor.add(CAS_neighbor_inner);
			InChiKey_neighbor.add(InChiKey_neighbor_inner);
			DTXSID_neighbor.add(DTXSID_neighbor_inner);
			DSSTOXMPID_neighbor.add(DSSTOXMPID_neighbor_inner);
			Exp_neighbor.add(Exp_neighbor_inner);
			pred_neighbor.add(pred_neighbor_inner);

			List<Double> pred_dc_array = new ArrayList<>();
			List<Double> pred_w_array = new ArrayList<>();
			boolean nanFound = false;
			for(int j = 0; j < pred.getDc().getColumnDimension(); j++) {
				if(!Double.isNaN(pred.getDc().getEntry(i, j))) {
					pred_dc_array.add(pred.getDc().getEntry(i, j) + 1);
					pred_w_array.add(pred.getW().getEntry(i, j));
				} else {
					nanFound = true;
				}
			}
			
			if(nanFound) {
				pred.getClassPredW().setEntry(i, 0, Double.NaN);
				AD_Model.setEntry(i, 0.0);
				AD_index.setEntry(i, 0.0);
				Conf_index.setEntry(i, 0.0);
			} else {
				Conf_index.setEntry(i, calculateConfIndex(pred, Utils.vectorFromStringList(pred_neighbor.get(i)), Utils.vectorFromStringList(Exp_neighbor.get(i)), AD_index, i));
			}
			
			//Populate results
			DefaultModelResult result = new DefaultModelResult();
			result.setChemicalDescriptors(descriptors.get(i));
			result.setPredictedValue(pred.getClassPred().getEntry(0, i) - 1);
			result.setAdModel((int)AD_Model.getEntry(i));
			result.setAdIndex(AD_index.getEntry(i));
			result.setConfidenceIndex(Conf_index.getEntry(i));
			result.setCasNeighbor(CAS_neighbor.get(i));
			result.setInchiNeighbor(InChiKey_neighbor.get(i));
			result.setDtxsidNeighbor(DTXSID_neighbor.get(i));
			result.setDsstoxmpidNeighbor(DSSTOXMPID_neighbor.get(i));
			result.setExpNeighbor(Exp_neighbor.get(i));
			result.setPredNeighbor(pred_neighbor.get(i));
			
			resultList.add(result);
		}
		
		return resultList;
	}

	@Override
	public Double calculateConfIndex(Prediction pred, RealVector predNeighbor, RealVector expNeighbor,
			RealVector ADIndex, int i) {
		Res rmse = null;
		try {
			rmse = CalcRegParam.calculate(expNeighbor, predNeighbor);
		} catch (Exception e) {
			return 0.0;
		}
		return (1.0 / (1 + rmse.getRMSEC()) + ADIndex.getEntry(i)) / 2.0;
	}
}
