package net.nabnet.time

import spock.lang.Specification

import java.time.format.DateTimeFormatter


/**
 * Created by nabuchi on 2016/11/06.
 */
class ExpandedLocalTimeTest extends Specification {
    def "should format with dateTimeFormatter"() {
        when:
        String actual = DateTimeFormatter.ISO_LOCAL_TIME.format(ExpandedLocalTime.of(28, 30, 0))
        then:
        actual == "28:30:00"
    }

    def "should parse with "() {
        when:
        ExpandedLocalTime expandedLocalTime = ExpandedLocalTime.parse("28:30:00", DateTimeFormatter.ISO_LOCAL_TIME)
        String actual = DateTimeFormatter.ISO_LOCAL_TIME.format(expandedLocalTime)
        then:
        actual == "28:30:00"
    }
}
