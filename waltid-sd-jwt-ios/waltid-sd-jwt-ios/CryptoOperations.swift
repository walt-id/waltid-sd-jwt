//
//  waltid_sd_jwt_ios.swift
//  waltid-sd-jwt-ios
//
//  Created by Ivan Pagac on 09/11/2023.
//

import Foundation
import JOSESwift

@objc
public class HMAC_Operations: NSObject, Operations {
    @objc
    public static func sign(body: String, alg: String, key: Data, typ: String, keyId: String? = nil) -> SignResult {
        return OperationsBase<Data>.sign(body: body, alg: alg, key: key, typ: typ)
    }

    @objc
    public static func verify(jws: String, key: Data) -> VerifyResult {
        return OperationsBase<Data>.verify(jws: jws, key: key)
    }
}

@objc
public class DS_Operations: NSObject, Operations {
    @objc
    public static func sign(body: String, alg: String, key: SecKey, typ: String, keyId: String? = nil) -> SignResult {
        return OperationsBase<SecKey>.sign(body: body, alg: alg, key: key, typ: typ)
    }

    @objc
    public static func verify(jws: String, key: SecKey) -> VerifyResult {
        return OperationsBase<SecKey>.verify(jws: jws, key: key)
    }
}
