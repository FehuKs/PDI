package src;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import src.infra.*;
import src.ui.Inputs;
import org.opencv.core.Mat;
import org.opencv.core.CvType;

class NativeAndInputsTest {
    @Test void nativeResourcesReleasedOnFailureAndScopesAreIndependent() {
        OpenCvRuntime.ensureLoaded();Mat matrix;
        try(CvScope outer=CvScope.open()){
            matrix=CvScope.track(Mat.ones(4,4,CvType.CV_8UC1));
            Mat innerMatrix;
            try(CvScope inner=CvScope.open()){innerMatrix=CvScope.track(Mat.ones(3,3,CvType.CV_8UC1));}
            assertTrue(innerMatrix.empty());assertFalse(matrix.empty());
        }
        assertTrue(matrix.empty());
        Mat failureMatrix=new Mat(2,2,CvType.CV_8UC1);
        assertThrows(IllegalStateException.class,()->{try(CvScope scope=CvScope.open()){CvScope.track(failureMatrix);throw new IllegalStateException("failure");}});
        assertTrue(failureMatrix.empty());
    }
    @Test void invalidNumbersDoNotSilentlyBecomeDefaults() {
        assertEquals(1.5,Inputs.decimal("1,5",.1,5));assertEquals(-50,Inputs.integer(" -50 ",-255,255));
        for(String input:new String[]{"NaN","Infinity","-Infinity","abc","0","1e300"})assertThrows(IllegalArgumentException.class,()->Inputs.decimal(input,.1,5));
        assertThrows(IllegalArgumentException.class,()->Inputs.integer("abc",-255,255));
        assertThrows(IllegalArgumentException.class,()->Inputs.integer("256",-255,255));
    }
}
