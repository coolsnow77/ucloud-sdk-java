/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sidooo.ucloud;

public enum Region
{
    CN_BJ1("cn-bj1"), // 北京一
    CN_BJ2("cn-bj2"), // 北京二
    CN_ZJ("cn-zj"),   // 浙江
    CN_SH("cn-sh"),   // 上海一
    CN_SH2("cn-sh2"), // 上海二
    CN_GD("cn-gd"),   // 广州
    HK("hk"),         // 香港
    US_CA("us-ca"),   // 洛杉矶
    US_WS("us-ws"),   // 华盛顿
    GE_FRA("ge-fra"), // 法兰克福
    TH_BKK("th-bkk"),     // 曼谷
    KR_SEOUL("kr-seoul"),    // 首尔
    SG("sg"),     // 新加坡
    TW_TP("tw-tp"),    // 台北
    TW_KH("tw-kh"),       // 高雄
    JPN_TKY("jpn-tky"), // 东京
    RUS_MOSC("rus-mosc"),      // 莫斯科
    UAE_DUBAI("uae-dubai"),     // 迪拜
    IDN_JAKARTA("idn-jakarta");     // 雅加达

    private String name;

    Region(String name)
    {
        this.name = name;
    }
}
