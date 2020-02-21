package com.fys;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        long[] ds = {1, 1024, 1024 * 1024, 1024 * 102 * 1024};
        String[] dw = {"bit", "B", "KB", "MB"};
        double inValue = 51;

        String in = "";
        for (int i = ds.length - 1; i >= 0; i--) {
            if (inValue / ds[i] >= 1) {
                in = inValue / ds[i] + dw[i];
                break;
            }
        }
        System.out.println(in);


    }

}
