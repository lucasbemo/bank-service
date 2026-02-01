package com.lz.bank.adapter.out.http;

import com.lz.bank.domain.port.AuthorizationPort;

public class MockAuthorizationAdapter implements AuthorizationPort {
    @Override
    public boolean authorize() {
        return true;
    }
}
