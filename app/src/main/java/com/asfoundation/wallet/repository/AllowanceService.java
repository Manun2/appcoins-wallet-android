package com.asfoundation.wallet.repository;

import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

import static org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction;

public class AllowanceService {

  private static final String DECIMALS = "1000000000000000000";

  private final Web3j web3j;

  public AllowanceService(Web3j web3j) {
    this.web3j = web3j;
  }

  private static Function allowance(String owner, String allowee) {
    return new Function("allowance", Arrays.asList(new Address(owner), new Address(allowee)),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));
  }

  public Single<BigDecimal> checkAllowance(String owner, String allowee, String tokenAddress) {
    return Single.fromCallable(() -> {
      Function function = allowance(owner, allowee);

      String responseValue = callSmartContractFunction(function, tokenAddress, owner);

      List<Type> response =
          FunctionReturnDecoder.decode(responseValue, function.getOutputParameters());
      if (response.size() == 1) {
        return new BigDecimal(((Uint256) response.get(0)).getValue()).divide(
            new BigDecimal(DECIMALS));
      } else {
        throw new IllegalStateException("Failed to execute contract call!");
      }
    });
  }

  private String callSmartContractFunction(Function function, String contractAddress,
      String walletAddress) throws Exception {
    String encodedFunction = FunctionEncoder.encode(function);
    org.web3j.protocol.core.methods.request.Transaction transaction =
        createEthCallTransaction(walletAddress, contractAddress, encodedFunction);
    return web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
        .send()
        .getValue();
  }
}
