// ***LICENSE*** This file is licensed under GPLv2 with Classpath Exception. See LICENSE file under project root for more info

package net.cassite.desktop.chara.plugin.tianxingchatbot;

import net.cassite.desktop.chara.chat.tianxing.TianxingTuling;
import net.cassite.desktop.chara.manager.ChatbotManager;
import net.cassite.desktop.chara.plugin.Plugin;

public class TianxingChatbotPlugin implements Plugin {
    @Override
    public String name() {
        return "TianxingChatbot";
    }

    @Override
    public int version() {
        return 1000000;
    }

    @Override
    public void launch() {
        ChatbotManager.register(new TianxingTuling());
    }

    @Override
    public void clicked() {
        // do nothing
    }

    @Override
    public void release() {
        // do nothing
    }
}
