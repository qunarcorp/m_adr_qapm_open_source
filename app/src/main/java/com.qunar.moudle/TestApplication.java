package com.qunar.moudle;

import com.mqunar.fastlocaldebug.DebugInitHelper;
import com.mqunar.fastlocaldebug.LocalDebugApplication;

/**
 * Created by wzx on 2019/1/24.
 */
public class TestApplication extends LocalDebugApplication {
    @Override
    protected void init() {
        super.init();

        //todo 快速配置指定Scheme
        DebugInitHelper.addScheme("qunaraphone://hy?xxx");

        //todo 自定义初始化代码

    }
}
