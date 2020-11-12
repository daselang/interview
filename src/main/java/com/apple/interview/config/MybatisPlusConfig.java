package com.apple.interview.config;

import com.apple.interview.dao.DaoPackage;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author xuzhongyu
 */
@Configuration
@EnableTransactionManagement(mode = AdviceMode.PROXY)
@MapperScan(basePackageClasses = {DaoPackage.class}, annotationClass = Repository.class)
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();

        /**
         * pagination
         */
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setMaxLimit(-1L);
        /**
         * OptimisticLocker
         *
         */
        OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor = new OptimisticLockerInnerInterceptor();

        mybatisPlusInterceptor.addInnerInterceptor(paginationInnerInterceptor);
        mybatisPlusInterceptor.addInnerInterceptor(optimisticLockerInnerInterceptor);
        return mybatisPlusInterceptor;
    }
}

