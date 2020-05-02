package club.gclmit.chaos.storage.exception;

import club.gclmit.chaos.core.exception.AbstractChaosException;

/**
 * <p>
 *  chaos 存储模块的异常处理
 * </p>
 *
 * @author: gclm
 * @date: 2020/1/15 3:46 下午
 * @version: V1.0
 * @since 1.8
 */
public class ChaosStorageException extends AbstractChaosException {

    public ChaosStorageException() {
    }

    public ChaosStorageException(String message) {
        super(message);
    }

    public ChaosStorageException(String messageTemplate, Object... params) {
        super(messageTemplate, params);
    }

    public ChaosStorageException(String message, Throwable cause) {
        super(message, cause);
    }

}
