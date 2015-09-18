package com.izettle.cart;

import java.io.Serializable;

public class ServiceChargeLine<S extends ServiceCharge<S>> implements Serializable {

    private final S serviceCharge;
    private final long value;
    private final Long vat;

    ServiceChargeLine(S serviceCharge, long value, Long vat) {
        this.serviceCharge = serviceCharge;
        this.value = value;
        this.vat = vat;
    }

    public S getServiceCharge() {
        return serviceCharge;
    }

    public long getValue() {
        return value;
    }

    public Long getVat() {
        return vat;
    }

    @Override
    public String toString() {
        return "ServiceChargeLine {" +
            "serviceCharge=" + serviceCharge +
            ", value=" + value +
            ", vat=" + vat +
            '}';
    }
}
