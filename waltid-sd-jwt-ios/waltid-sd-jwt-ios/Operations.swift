//
//  Operations.swift
//  waltid-sd-jwt-ios
//
//  Created by Ivan Pagac on 10/11/2023.
//

import Foundation
import JOSESwift

protocol Operations {
    associatedtype KeyType
    static func sign(body :String, alg: String, key: KeyType, typ: String, keyId: String?) -> SignResult
    static func verify(jws: String, key: KeyType) -> VerifyResult
}

@objc
public class SignResult: NSObject {
    @objc
    public var success: Bool {
        errorMessage == nil
    }

    @objc
    public let data: String?

    @objc
    public let errorMessage: String?

    private init(data: String? = nil, errorMessage: String? = nil) {
        self.data = data
        self.errorMessage = errorMessage
    }

    static func with(success data: String) -> SignResult {
        SignResult(data: data)
    }

    static func with(failure message: String) -> SignResult {
        SignResult(errorMessage: message)
    }
}

@objc
public class VerifyResult: NSObject {
    @objc
    public var success: Bool {
        errorMessage == nil
    }

    @objc
    public let isValid: Bool

    @objc
    public let errorMessage: String?

    private init(isValid: Bool = false, errorMessage: String? = nil) {
        self.isValid = isValid
        self.errorMessage = errorMessage
    }

    static func with(isValid: Bool) -> VerifyResult {
        VerifyResult(isValid: isValid)
    }

    static func with(failure message: String) -> VerifyResult {
        VerifyResult(errorMessage: message)
    }
}

class OperationsBase<TKeyType>: NSObject, Operations {
    public static func sign(body payload: String, alg: String, key: TKeyType, typ: String, keyId: String? = nil) -> SignResult {
        do {
            guard let signingAlgorithm = JOSESwift.SignatureAlgorithm(rawValue: alg) else {
                return SignResult.with(failure: "Unknown signature algorithm")
            }

            guard let signer = Signer(signingAlgorithm: signingAlgorithm, key: key) else {
                return SignResult.with(failure: "Could not construct signer.")
            }

            guard let payloadData = payload.data(using: .utf8) else {
                return SignResult.with(failure: "Body not in UTF-8")
            }

            var header = JWSHeader(algorithm: signingAlgorithm)
            header.typ = typ
            if let keyId {
                header.kid = keyId
            }

            let jws = try JWS(header: header, payload: Payload(payloadData), signer: signer)
            return SignResult.with(success: jws.compactSerializedString)
        } catch {
            return SignResult.with(failure: "Could not perform Sign, reason: \(error.localizedDescription).")
        }
    }

    public static func verify(jws: String, key: TKeyType) -> VerifyResult {
        do {
            let jws = try JWS(compactSerialization: jws)

            guard let algorithm = jws.header.algorithm else {
                return VerifyResult.with(failure: "JWS does not contain alg header or was not recognized. This header must be present and be valid.")
            }

            guard let verifier = Verifier(verifyingAlgorithm: algorithm, key: key) else {
                return VerifyResult.with(failure: "Could not construct verifier with the provided key.")
            }

            return VerifyResult.with(isValid: jws.isValid(for: verifier))
        } catch {
            return VerifyResult.with(failure: "Could not perform Verify, reason: \(error.localizedDescription)")
        }
    }
}
