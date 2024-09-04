package it.silvio.xml_to_json_converter.config;

import java.io.File;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.file.DirectoryScanner;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.RecursiveDirectoryScanner;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.transaction.DefaultTransactionSynchronizationFactory;
import org.springframework.integration.transaction.ExpressionEvaluatingTransactionSynchronizationProcessor;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.integration.transaction.TransactionSynchronizationFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Configuration
public class FilePollingIntegrationFlow {

    @Bean(name = "inboundReadDirectory")
    public File inboundReadDirectory(@Value("${workflow.directories.input}") String path) {
        return makeDirectory(path);
    }

    @Bean(name = "inboundProcessedDirectory")
    public File inboundProcessedDirectory(@Value("${workflow.directories.processed}") String path) {
        return makeDirectory(path);
    }

    @Bean(name = "inboundFailedDirectory")
    public File inboundFailedDirectory(@Value("${workflow.directories.failed}") String path) {
        return makeDirectory(path);
    }

    @Bean(name = "inboundOutDirectory")
    public File inboundOutDirectory(@Value("${workflow.directories.output}") String path) {
        return makeDirectory(path);
    }

    private File makeDirectory(String path) {
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    @Autowired
    private File inboundReadDirectory;

    @Bean
    public MessageChannel fileInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public DirectoryScanner directoryScanner(@Value("${inbound.filename.regex}") String regex) {
        DirectoryScanner scanner = new RecursiveDirectoryScanner();
        CompositeFileListFilter<File> filter = new CompositeFileListFilter<>(
                Arrays.asList(new AcceptOnceFileListFilter<>(),
                        new RegexPatternFileListFilter(regex)));
        scanner.setFilter(filter);
        return scanner;
    }

    @Bean
    public FileReadingMessageSource fileReadingMessageSource(DirectoryScanner directoryScanner) {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(this.inboundReadDirectory);
        source.setScanner(directoryScanner);
        source.setAutoCreateDirectory(true);
        return source;
    }

    @Bean
    PseudoTransactionManager transactionManager() {
        return new PseudoTransactionManager();
    }

    @Bean
    TransactionSynchronizationFactory transactionSynchronizationFactory() {
        ExpressionParser parser = new SpelExpressionParser();
        ExpressionEvaluatingTransactionSynchronizationProcessor syncProcessor = new ExpressionEvaluatingTransactionSynchronizationProcessor();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        syncProcessor.setBeanFactory(beanFactory);
        syncProcessor.setAfterCommitExpression(
                parser.parseExpression("payload.renameTo(new java.io.File(@inboundProcessedDirectory.path "
                        + " + T(java.io.File).separator + payload.name))"));
        syncProcessor.setAfterRollbackExpression(
                parser.parseExpression("payload.renameTo(new java.io.File(@inboundFailedDirectory.path "
                        + " + T(java.io.File).separator + payload.name))"));
        return new DefaultTransactionSynchronizationFactory(syncProcessor);
    }
}