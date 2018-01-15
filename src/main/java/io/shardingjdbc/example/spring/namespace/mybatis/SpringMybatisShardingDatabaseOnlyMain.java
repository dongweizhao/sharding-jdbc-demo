/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.example.spring.namespace.mybatis;

import io.shardingjdbc.example.spring.namespace.mybatis.service.DemoService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *请求1
 1、node1 update t_order set status='5' where status='2'

 4、node2 update t_order set status='5' where status='2'
 请求2
 2、node1  update t_order set status='5' where status='2'
 3、node2  update t_order set status='5' where status='2'
 *
 * 如果按照以上顺序执行时,会不会发生死锁现象
 */
public final class SpringMybatisShardingDatabaseOnlyMain {

    public static void main(final String[] args) throws SQLException, InterruptedException {
        final ApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/mybatisShardingDatabaseOnlyContext.xml");
        DemoService demoService = applicationContext.getBean(DemoService.class);
        demoService.drop();
        demoService.demo();
        ExecutorService executors = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 2; i++) {
            executors.execute(new UpdateRunable(applicationContext));
        }
        TimeUnit.SECONDS.sleep(2);
        executors.shutdown();
        while (!executors.isTerminated()){
        }
        System.out.println("程序结束");

    }


    public static class UpdateRunable implements Runnable {
        ApplicationContext applicationContext;

        public UpdateRunable(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public void run() {
                DataSource dataSource = (DataSource) applicationContext.getBean("shardingDataSource");
                Connection connection = null;
                PreparedStatement pre = null;
                try {
                    connection = dataSource.getConnection();
                    connection.setAutoCommit(false);
                    pre = connection.prepareStatement("update t_order set status='5' where status='2' ");
                    int c = pre.executeUpdate();
                    System.out.println("t-name:" + Thread.currentThread().getName() + "  c1:" + c);
                    connection.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

}
