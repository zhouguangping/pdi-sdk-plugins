package org.pentaho.di.sdk.samples.modbus.input.utils;

public class ModbusMaTransform {
    public static double transform(double h ,double l ,double value ) {
        double d = 0;
        if(value>20) {
            d=20;
        }else if(value<4) {
            d=4;
        }else {
            d=value;
        }
        double r = (h-l)*(d-4)/16 +l;
        return r;
    }
}
