package com.appcoins.wallet.core.network.eskills.utils.utils;

/**
 * Created
 */
public abstract class BaseException extends RuntimeException {
  public BaseException() {
  }

  public BaseException(String detailMessage) {
    super(detailMessage);
  }

  public BaseException(Throwable throwable) {
    super(throwable);
  }
}
