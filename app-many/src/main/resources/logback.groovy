import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.classic.filter.ThresholdFilter

import ch.qos.logback.core.status.OnConsoleStatusListener

import ch.qos.logback.classic.Level
import static ch.qos.logback.classic.Level.*

statusListener(OnConsoleStatusListener)

def defaultPattern = '%d{HH:mm:ss.SSS} %-5level %logger{5} - %msg%n%ex{short}'
def LOG_FILE = '../logs'+File.separator

appender('FILE', RollingFileAppender) {
    file = LOG_FILE+'ads-admin.log'
    encoder(PatternLayoutEncoder) { Pattern = defaultPattern}
    rollingPolicy(TimeBasedRollingPolicy) { FileNamePattern = LOG_FILE+'ads-%d{yyyyMMdd}.zip' }
}

appender('CONSOLE', ConsoleAppender) {
    encoder(PatternLayoutEncoder) { pattern = defaultPattern; }
}

root(WARN, ['FILE', 'CONSOLE'])
logger('com.glodon', DEBUG, ['FILE', 'CONSOLE'],false)
