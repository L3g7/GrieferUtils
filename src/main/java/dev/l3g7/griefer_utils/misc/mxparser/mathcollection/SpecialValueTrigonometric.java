package dev.l3g7.griefer_utils.misc.mxparser.mathcollection;

import java.util.ArrayList;
import java.util.List;

public class SpecialValueTrigonometric {

	public static double EPSILON = 10 * BinaryRelations.DEFAULT_COMPARISON_EPSILON;
	public static double SIN_0 = 0.0;
	public static double COS_0 = 1.0;
	public static double TAN_0 = 0.0;
	public static double CTAN_0 = Double.NaN;
	public static double SEC_0 = 1.0;
	public static double CSC_0 = Double.NaN;
	public static double SIN_30 = 0.5;
	public static double COS_30 = MathConstants.SQRT3BY2;
	public static double TAN_30 = MathConstants.SQRT3BY3;
	public static double CTAN_30 = MathConstants.SQRT3;
	public static double SEC_30 = MathConstants.D2BYSQRT3;
	public static double CSC_30 = 2.0;
	public static double SIN_45 = MathConstants.SQRT2BY2;
	public static double COS_45 = MathConstants.SQRT2BY2;
	public static double TAN_45 = 1.0;
	public static double CTAN_45 = 1.0;
	public static double SEC_45 = MathConstants.SQRT2;
	public static double CSC_45 = MathConstants.SQRT2;
	public static double SIN_60 = MathConstants.SQRT3BY2;
	public static double COS_60 = 0.5;
	public static double TAN_60 = MathConstants.SQRT3;
	public static double CTAN_60 = MathConstants.SQRT3BY3;
	public static double SEC_60 = 2.0;
	public static double CSC_60 = MathConstants.D2BYSQRT3;
	public static double SIN_90 = 1.0;
	public static double COS_90 = 0.0;
	public static double TAN_90 = Double.NaN;
	public static double CTAN_90 = 0;
	public static double SEC_90 = Double.NaN;
	public static double CSC_90 = 1.0;
	public static double SIN_120 = SIN_60;
	public static double COS_120 = -COS_60;
	public static double TAN_120 = -TAN_60;
	public static double CTAN_120 = -CTAN_60;
	public static double SEC_120 = -SEC_60;
	public static double CSC_120 = CSC_60;
	public static double SIN_135 = SIN_45;
	public static double COS_135 = -COS_45;
	public static double TAN_135 = -TAN_45;
	public static double CTAN_135 = -CTAN_45;
	public static double SEC_135 = -SEC_45;
	public static double CSC_135 = CSC_45;
	public static double SIN_150 = SIN_30;
	public static double COS_150 = -COS_30;
	public static double TAN_150 = -TAN_30;
	public static double CTAN_150 = -CTAN_30;
	public static double SEC_150 = -SEC_30;
	public static double CSC_150 = CSC_30;
	public static double SIN_180 = SIN_0;
	public static double COS_180 = -COS_0;
	public static double TAN_180 = TAN_0;
	public static double CTAN_180 = CTAN_0;
	public static double SEC_180 = -SEC_0;
	public static double CSC_180 = CSC_0;
	public static double SIN_210 = -SIN_30;
	public static double COS_210 = -COS_30;
	public static double TAN_210 = TAN_30;
	public static double CTAN_210 = CTAN_30;
	public static double SEC_210 = -SEC_30;
	public static double CSC_210 = -CSC_30;
	public static double SIN_225 = -SIN_45;
	public static double COS_225 = -COS_45;
	public static double TAN_225 = TAN_45;
	public static double CTAN_225 = CTAN_45;
	public static double SEC_225 = -SEC_45;
	public static double CSC_225 = -CSC_45;
	public static double SIN_240 = -SIN_60;
	public static double COS_240 = -COS_60;
	public static double TAN_240 = TAN_60;
	public static double CTAN_240 = CTAN_60;
	public static double SEC_240 = -SEC_60;
	public static double CSC_240 = -CSC_60;
	public static double SIN_270 = -SIN_90;
	public static double COS_270 = COS_90;
	public static double TAN_270 = TAN_90;
	public static double CTAN_270 = CTAN_90;
	public static double SEC_270 = SEC_90;
	public static double CSC_270 = -CSC_90;
	public static double SIN_300 = -SIN_60;
	public static double COS_300 = COS_60;
	public static double TAN_300 = -TAN_60;
	public static double CTAN_300 = -CTAN_60;
	public static double SEC_300 = SEC_60;
	public static double CSC_300 = -CSC_60;
	public static double SIN_315 = -SIN_45;
	public static double COS_315 = COS_45;
	public static double TAN_315 = -TAN_45;
	public static double CTAN_315 = -CTAN_45;
	public static double SEC_315 = SEC_45;
	public static double CSC_315 = -CSC_45;
	public static double SIN_330 = -SIN_30;
	public static double COS_330 = COS_30;
	public static double TAN_330 = -TAN_30;
	public static double CTAN_330 = -CTAN_30;
	public static double SEC_330 = SEC_30;
	public static double CSC_330 = -CSC_30;
	public static double SIN_360 = SIN_0;
	public static double COS_360 = COS_0;
	public static double TAN_360 = TAN_0;
	public static double CTAN_360 = CTAN_0;
	public static double SEC_360 = SEC_0;
	public static double CSC_360 = CSC_0;

	public static SpecialValueTrigonometric[] valuesListTrig = {
			new SpecialValueTrigonometric( 0.0, SIN_0, COS_0, TAN_0, CTAN_0, SEC_0, CSC_0 )
			,new SpecialValueTrigonometric( 2.0, SIN_0, COS_0, TAN_0, CTAN_0, SEC_0, CSC_0 )
			,new SpecialValueTrigonometric( -2.0, SIN_0, COS_0, TAN_0, CTAN_0, SEC_0, CSC_0 )
			,new SpecialValueTrigonometric( 4.0, SIN_0, COS_0, TAN_0, CTAN_0, SEC_0, CSC_0 )
			,new SpecialValueTrigonometric( -4.0, SIN_0, COS_0, TAN_0, CTAN_0, SEC_0, CSC_0 )
			,new SpecialValueTrigonometric( (1.0 / 6.0), SIN_30, COS_30, TAN_30, CTAN_30, SEC_30, CSC_30 )
			,new SpecialValueTrigonometric( (13.0 / 6.0), SIN_30, COS_30, TAN_30, CTAN_30, SEC_30, CSC_30 )
			,new SpecialValueTrigonometric( (-11.0 / 6.0), SIN_30, COS_30, TAN_30, CTAN_30, SEC_30, CSC_30 )
			,new SpecialValueTrigonometric( (25.0 / 6.0), SIN_30, COS_30, TAN_30, CTAN_30, SEC_30, CSC_30 )
			,new SpecialValueTrigonometric( (-23.0 / 6.0), SIN_30, COS_30, TAN_30, CTAN_30, SEC_30, CSC_30 )
			,new SpecialValueTrigonometric( (1.0 / 4.0), SIN_45, COS_45, TAN_45, CTAN_45, SEC_45, CSC_45 )
			,new SpecialValueTrigonometric( (9.0 / 4.0), SIN_45, COS_45, TAN_45, CTAN_45, SEC_45, CSC_45 )
			,new SpecialValueTrigonometric( (-7.0 / 4.0), SIN_45, COS_45, TAN_45, CTAN_45, SEC_45, CSC_45 )
			,new SpecialValueTrigonometric( (17.0 / 4.0), SIN_45, COS_45, TAN_45, CTAN_45, SEC_45, CSC_45 )
			,new SpecialValueTrigonometric( (-15.0 / 4.0), SIN_45, COS_45, TAN_45, CTAN_45, SEC_45, CSC_45 )
			,new SpecialValueTrigonometric( (1.0 / 3.0), SIN_60, COS_60, TAN_60, CTAN_60, SEC_60, CSC_60 )
			,new SpecialValueTrigonometric( (7.0 / 3.0), SIN_60, COS_60, TAN_60, CTAN_60, SEC_60, CSC_60 )
			,new SpecialValueTrigonometric( (-5.0 / 3.0), SIN_60, COS_60, TAN_60, CTAN_60, SEC_60, CSC_60 )
			,new SpecialValueTrigonometric( (13.0 / 3.0), SIN_60, COS_60, TAN_60, CTAN_60, SEC_60, CSC_60 )
			,new SpecialValueTrigonometric( (-11.0 / 3.0), SIN_60, COS_60, TAN_60, CTAN_60, SEC_60, CSC_60 )
			,new SpecialValueTrigonometric( (1.0 / 2.0), SIN_90, COS_90, TAN_90, CTAN_90, SEC_90, CSC_90 )
			,new SpecialValueTrigonometric( (5.0 / 2.0), SIN_90, COS_90, TAN_90, CTAN_90, SEC_90, CSC_90 )
			,new SpecialValueTrigonometric( (-3.0 / 2.0), SIN_90, COS_90, TAN_90, CTAN_90, SEC_90, CSC_90 )
			,new SpecialValueTrigonometric( (9.0 / 2.0), SIN_90, COS_90, TAN_90, CTAN_90, SEC_90, CSC_90 )
			,new SpecialValueTrigonometric( (-7.0 / 2.0), SIN_90, COS_90, TAN_90, CTAN_90, SEC_90, CSC_90 )
			,new SpecialValueTrigonometric( (2.0 / 3.0), SIN_120, COS_120, TAN_120, CTAN_120, SEC_120, CSC_120 )
			,new SpecialValueTrigonometric( (8.0 / 3.0), SIN_120, COS_120, TAN_120, CTAN_120, SEC_120, CSC_120 )
			,new SpecialValueTrigonometric( (-4.0 / 3.0), SIN_120, COS_120, TAN_120, CTAN_120, SEC_120, CSC_120 )
			,new SpecialValueTrigonometric( (14.0 / 3.0), SIN_120, COS_120, TAN_120, CTAN_120, SEC_120, CSC_120 )
			,new SpecialValueTrigonometric( (-10.0 / 3.0), SIN_120, COS_120, TAN_120, CTAN_120, SEC_120, CSC_120 )
			,new SpecialValueTrigonometric( (3.0 / 4.0), SIN_135, COS_135, TAN_135, CTAN_135, SEC_135, CSC_135 )
			,new SpecialValueTrigonometric( (11.0 / 4.0), SIN_135, COS_135, TAN_135, CTAN_135, SEC_135, CSC_135 )
			,new SpecialValueTrigonometric( (-5.0 / 4.0), SIN_135, COS_135, TAN_135, CTAN_135, SEC_135, CSC_135 )
			,new SpecialValueTrigonometric( (19.0 / 4.0), SIN_135, COS_135, TAN_135, CTAN_135, SEC_135, CSC_135 )
			,new SpecialValueTrigonometric( (-13.0 / 4.0), SIN_135, COS_135, TAN_135, CTAN_135, SEC_135, CSC_135 )
			,new SpecialValueTrigonometric( (5.0 / 6.0), SIN_150, COS_150, TAN_150, CTAN_150, SEC_150, CSC_150 )
			,new SpecialValueTrigonometric( (17.0 / 6.0), SIN_150, COS_150, TAN_150, CTAN_150, SEC_150, CSC_150 )
			,new SpecialValueTrigonometric( (-7.0 / 6.0), SIN_150, COS_150, TAN_150, CTAN_150, SEC_150, CSC_150 )
			,new SpecialValueTrigonometric( (29.0 / 6.0), SIN_150, COS_150, TAN_150, CTAN_150, SEC_150, CSC_150 )
			,new SpecialValueTrigonometric( (-19.0 / 6.0), SIN_150, COS_150, TAN_150, CTAN_150, SEC_150, CSC_150 )
			,new SpecialValueTrigonometric( 1.0, SIN_180, COS_180, TAN_180, CTAN_180, SEC_180, CSC_180 )
			,new SpecialValueTrigonometric( 3.0, SIN_180, COS_180, TAN_180, CTAN_180, SEC_180, CSC_180 )
			,new SpecialValueTrigonometric( -1.0, SIN_180, COS_180, TAN_180, CTAN_180, SEC_180, CSC_180 )
			,new SpecialValueTrigonometric( 5.0, SIN_180, COS_180, TAN_180, CTAN_180, SEC_180, CSC_180 )
			,new SpecialValueTrigonometric( -3.0, SIN_180, COS_180, TAN_180, CTAN_180, SEC_180, CSC_180 )
			,new SpecialValueTrigonometric( (7.0 / 6.0), SIN_210, COS_210, TAN_210, CTAN_210, SEC_210, CSC_210 )
			,new SpecialValueTrigonometric( (19.0 / 6.0), SIN_210, COS_210, TAN_210, CTAN_210, SEC_210, CSC_210 )
			,new SpecialValueTrigonometric( (-5.0 / 6.0), SIN_210, COS_210, TAN_210, CTAN_210, SEC_210, CSC_210 )
			,new SpecialValueTrigonometric( (31.0 / 6.0), SIN_210, COS_210, TAN_210, CTAN_210, SEC_210, CSC_210 )
			,new SpecialValueTrigonometric( (-17.0 / 6.0), SIN_210, COS_210, TAN_210, CTAN_210, SEC_210, CSC_210 )
			,new SpecialValueTrigonometric( (5.0 / 4.0), SIN_225, COS_225, TAN_225, CTAN_225, SEC_225, CSC_225 )
			,new SpecialValueTrigonometric( (13.0 / 4.0), SIN_225, COS_225, TAN_225, CTAN_225, SEC_225, CSC_225 )
			,new SpecialValueTrigonometric( (-3.0 / 4.0), SIN_225, COS_225, TAN_225, CTAN_225, SEC_225, CSC_225 )
			,new SpecialValueTrigonometric( (21.0 / 4.0), SIN_225, COS_225, TAN_225, CTAN_225, SEC_225, CSC_225 )
			,new SpecialValueTrigonometric( (-11.0 / 4.0), SIN_225, COS_225, TAN_225, CTAN_225, SEC_225, CSC_225 )
			,new SpecialValueTrigonometric( (4.0 / 3.0), SIN_240, COS_240, TAN_240, CTAN_240, SEC_240, CSC_240 )
			,new SpecialValueTrigonometric( (10.0 / 3.0), SIN_240, COS_240, TAN_240, CTAN_240, SEC_240, CSC_240 )
			,new SpecialValueTrigonometric( (-2.0 / 3.0), SIN_240, COS_240, TAN_240, CTAN_240, SEC_240, CSC_240 )
			,new SpecialValueTrigonometric( (16.0 / 3.0), SIN_240, COS_240, TAN_240, CTAN_240, SEC_240, CSC_240 )
			,new SpecialValueTrigonometric( (-8.0 / 3.0), SIN_240, COS_240, TAN_240, CTAN_240, SEC_240, CSC_240 )
			,new SpecialValueTrigonometric( (3.0 / 2.0), SIN_270, COS_270, TAN_270, CTAN_270, SEC_270, CSC_270 )
			,new SpecialValueTrigonometric( (7.0 / 2.0), SIN_270, COS_270, TAN_270, CTAN_270, SEC_270, CSC_270 )
			,new SpecialValueTrigonometric( (-1.0 / 2.0), SIN_270, COS_270, TAN_270, CTAN_270, SEC_270, CSC_270 )
			,new SpecialValueTrigonometric( (11.0 / 2.0), SIN_270, COS_270, TAN_270, CTAN_270, SEC_270, CSC_270 )
			,new SpecialValueTrigonometric( (-5.0 / 2.0), SIN_270, COS_270, TAN_270, CTAN_270, SEC_270, CSC_270 )
			,new SpecialValueTrigonometric( (5.0 / 3.0), SIN_300, COS_300, TAN_300, CTAN_300, SEC_300, CSC_300 )
			,new SpecialValueTrigonometric( (11.0 / 3.0), SIN_300, COS_300, TAN_300, CTAN_300, SEC_300, CSC_300 )
			,new SpecialValueTrigonometric( (-1.0 / 3.0), SIN_300, COS_300, TAN_300, CTAN_300, SEC_300, CSC_300 )
			,new SpecialValueTrigonometric( (17.0 / 3.0), SIN_300, COS_300, TAN_300, CTAN_300, SEC_300, CSC_300 )
			,new SpecialValueTrigonometric( (-7.0 / 3.0), SIN_300, COS_300, TAN_300, CTAN_300, SEC_300, CSC_300 )
			,new SpecialValueTrigonometric( (7.0 / 4.0), SIN_315, COS_315, TAN_315, CTAN_315, SEC_315, CSC_315 )
			,new SpecialValueTrigonometric( (15.0 / 4.0), SIN_315, COS_315, TAN_315, CTAN_315, SEC_315, CSC_315 )
			,new SpecialValueTrigonometric( (-1.0 / 4.0), SIN_315, COS_315, TAN_315, CTAN_315, SEC_315, CSC_315 )
			,new SpecialValueTrigonometric( (23.0 / 4.0), SIN_315, COS_315, TAN_315, CTAN_315, SEC_315, CSC_315 )
			,new SpecialValueTrigonometric( (-9.0 / 4.0), SIN_315, COS_315, TAN_315, CTAN_315, SEC_315, CSC_315 )
			,new SpecialValueTrigonometric( (11.0 / 6.0), SIN_330, COS_330, TAN_330, CTAN_330, SEC_330, CSC_330 )
			,new SpecialValueTrigonometric( (23.0 / 6.0), SIN_330, COS_330, TAN_330, CTAN_330, SEC_330, CSC_330 )
			,new SpecialValueTrigonometric( (-1.0 / 6.0), SIN_330, COS_330, TAN_330, CTAN_330, SEC_330, CSC_330 )
			,new SpecialValueTrigonometric( (35.0 / 6.0), SIN_330, COS_330, TAN_330, CTAN_330, SEC_330, CSC_330 )
			,new SpecialValueTrigonometric( (-13.0 / 6.0), SIN_330, COS_330, TAN_330, CTAN_330, SEC_330, CSC_330 )
			,new SpecialValueTrigonometric( 6.0, SIN_360, COS_360, TAN_360, CTAN_360, SEC_360, CSC_360 )
	};

	public static List<SpecialValue> valuesListAsin;
	public static List<SpecialValue> valuesListAcos;
	public static List<SpecialValue> valuesListAtan;
	public static List<SpecialValue> valuesListActan;
	public static List<SpecialValue> valuesListAsec;
	public static List<SpecialValue> valuesListAcsc;

	public double factor;
	public double xrad;
	public double xdeg;
	public double xradFrom;
	public double xradTo;
	public double sin;
	public double cos;
	public double tan;
	public double ctan;
	public double sec;
	public double csc;

	public SpecialValueTrigonometric(double factor, double sin, double cos, double tan, double ctan, double sec, double csc) {
		this.factor = factor;
		this.xrad = factor * MathConstants.PI;
		this.xdeg = MathFunctions.round(factor * 180.0, 0);
		this.sin = sin;
		this.cos = cos;
		this.tan = tan;
		this.ctan = ctan;
		this.sec = sec;
		this.csc = csc;
		xradFrom = xrad - EPSILON;
		xradTo = xrad + EPSILON;

		if ( (-MathConstants.PIBY2 - EPSILON <= xrad) && (xrad <= MathConstants.PIBY2 + EPSILON) ) {
			if (valuesListAsin == null) valuesListAsin = new ArrayList<>();
			if (valuesListAtan == null) valuesListAtan = new ArrayList<>();
			if (valuesListAcsc == null) valuesListAcsc = new ArrayList<>();
			valuesListAsin.add(new SpecialValue(sin, xrad, xdeg));
			valuesListAtan.add(new SpecialValue(tan, xrad, xdeg));
			valuesListAcsc.add(new SpecialValue(csc, xrad, xdeg));
		}


		if ( (-EPSILON <= xrad) && (xrad <= MathConstants.PI + EPSILON) ) {
			if (valuesListAcos == null) valuesListAcos = new ArrayList<>();
			if (valuesListActan == null) valuesListActan = new ArrayList<>();
			if (valuesListAsec == null) valuesListAsec = new ArrayList<>();
			valuesListAcos.add(new SpecialValue(cos, xrad, xdeg));
			valuesListActan.add(new SpecialValue(ctan, xrad, xdeg));
			valuesListAsec.add(new SpecialValue(sec, xrad, xdeg));
		}

	}
	public static SpecialValueTrigonometric getSpecialValueTrigonometric(double xrad) {
		if (Double.isNaN(xrad)) return null;
		if (Double.isInfinite(xrad)) return null;
		for (SpecialValueTrigonometric sv : valuesListTrig) {
			if ( (sv.xradFrom <= xrad) && (xrad <= sv.xradTo) ) return sv;
		}
		return null;
	}
	public static SpecialValue getSpecialValueAsin(double x) {
		return getSpecialValue(x, valuesListAsin);
	}
	public static SpecialValue getSpecialValueAcos(double x) {
		return getSpecialValue(x, valuesListAcos);
	}
	public static SpecialValue getSpecialValueAtan(double x) {
		return getSpecialValue(x, valuesListAtan);
	}
	public static SpecialValue getSpecialValueActan(double x) {
		return getSpecialValue(x, valuesListActan);
	}
	public static SpecialValue getSpecialValueAsec(double x) {
		return getSpecialValue(x, valuesListAsec);
	}
	public static SpecialValue getSpecialValueAcsc(double x) {
		return getSpecialValue(x, valuesListAcsc);
	}
	private static SpecialValue getSpecialValue(double x, List<SpecialValue> valuesList) {
		if (Double.isNaN(x)) return null;
		if (Double.isInfinite(x)) return null;
		for (SpecialValue sv : valuesList) {
			if ( (sv.xFrom <= x) && (x <= sv.xTo) ) return sv;
		}
		return null;
	}
}
