package club.gclmit.chaos.core;

import club.gclmit.chaos.core.helper.IDHelper;

/**
 * <p>
 * ID 生成器
 * </p>
 *
 * @author: gclm
 * @date: 2019/11/7 0:17
 * @version: V1.0
 */
public class IDHelperTest {

    public static void main(String[] args) {
        System.out.println(IDHelper.getLongId());
        System.out.println(IDHelper.getStringId());
    }
}
