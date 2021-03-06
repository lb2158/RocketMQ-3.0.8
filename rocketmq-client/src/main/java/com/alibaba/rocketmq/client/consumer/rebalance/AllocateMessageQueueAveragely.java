/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
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
package com.alibaba.rocketmq.client.consumer.rebalance;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * 平均分配队列算法
 * 
 * @author fuchong<yubao.fyb@alibaba-inc.com>
 * @author manhong.yqd<manhong.yqd@taobao.com>
 * @since 2013-7-24
 */
public class AllocateMessageQueueAveragely implements AllocateMessageQueueStrategy {
    @Override
    public List<MessageQueue> allocate(String currentCID, List<MessageQueue> mqAll, List<String> cidAll) {
    	/*
    	 * chen.si 参数很重要，这里把接口的方法参数 引用到这里：
    	 * 			currentCID  当前ConsumerId
    	 * 			mqAll 		当前Topic的所有队列集合，无重复数据，且有序
    	 * 			cidAll 		当前订阅组的所有Consumer集合，无重复数据，且有序
    	 * 			return 		分配结果，无重复数据
    	 */
        if (currentCID == null || currentCID.length() < 1) {
            throw new IllegalArgumentException("currentCID is empty");
        }
        if (mqAll == null || mqAll.size() < 1) {
            throw new IllegalArgumentException("mqAll is null or mqAll size < 1");
        }
        if (cidAll == null || cidAll.size() < 1) {
            throw new IllegalArgumentException("cidAll is null or cidAll size < 1");
        }

        List<MessageQueue> result = new ArrayList<MessageQueue>();
        if (!cidAll.contains(currentCID)) { // 不存在此ConsumerId ,直接返回
            return result;
        }

        int index = cidAll.indexOf(currentCID);
        int mod = mqAll.size() % cidAll.size();
        int averageSize =
                mqAll.size() <= cidAll.size() ? 1 : (mod > 0 && index < mod ? mqAll.size() / cidAll.size()
                        + 1 : mqAll.size() / cidAll.size());
        int startIndex = (mod > 0 && index < mod) ? index * averageSize : index * averageSize + mod;
        int range = Math.min(averageSize, mqAll.size() - startIndex);
        for (int i = 0; i < range; i++) {
            result.add(mqAll.get((startIndex + i) % mqAll.size()));
        }
        return result;
    }
}
