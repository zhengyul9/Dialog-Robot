package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.utils.DateUtils;
import org.springframework.stereotype.Component;

/**
 * Implementation of the <tt>RegexAnnotator</tt> abstract class.
 * This annotator can annotate <Strong>date point</Strong> in queryAct,
 * before witch, the queryAct should already be annotated with
 * <tt>NumAnnotator</tt>.
 *
 * <p>During initialization, it will construct a map from function's name
 * to an lambda expression. Then, read the number regex file into memory.
 *
 * <p>Annotated data structure is {@link DateUtils.Date}, where <tt>type</tt>
 * should be check first to determine the lowest date unit level available. Then,
 * programmer can acquire the upper level date units in need.
 * For example, if type is DateType.DAY, this implies that Date.year, Date.month,
 * and Date.day is all set meaningfully. In contrast, if type is DateType.WEEK,
 * then only Date.year and Date.week is set, cause the month can not be determined
 * (consider when one week cross two months).
 *
 * <p>
 * This is an example of the annotator's usage:
 * <pre>
 *     act.query = 2017年8月1日,和一七年9月
 *     act.pQuery = {{数字}}年{{数字}}月{{数字}}日,和一七年{{数字}}月
 *     act.slots = { 数字: [2017, 8, 1, 9] }
 *     -> DateAnnotator.annotate(act) ->
 *     act.pQuery == {{日期}},和{{日期}}
 *     act.slots == { 日期: [DateUtils.Date(type: DateType.DAY, year: 2017, month: 8, day: 1),
 *                          DateUtils.Date(type: DateType.MONTH, year: 2017, month: 9, day: 0] }
 * </pre>
 *
 * @author Yuqi
 * @see RegexAnnotator
 * @since 1.8
 */
@Component("dateAnnotator")
public class DateAnnotator extends RegexAnnotator {


    public DateAnnotator() {

        /* construct map from label to lambda function */
        labelMap.put("date(yearString)",
                valuePack -> DateUtils.yStrDate(valuePack.matched));
        labelMap.put("date(y)",
                valuePack -> DateUtils.yDate(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("thisYearMDate(m)",
                valuePack -> DateUtils.thisYearMDate(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("date(m)",
                valuePack -> DateUtils.mDate(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("date(d)",
                valuePack -> DateUtils.dDate(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("date(m,d)",
                valuePack -> DateUtils.mdDate(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("date(y,m)",
                valuePack -> DateUtils.ymDate(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("date(ymString,m)",
                valuePack -> DateUtils.ymStrDate(valuePack.matched,
                        ((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("date(y,m,d)",
                valuePack -> DateUtils.ymdDate(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue(),
                        ((Double) valuePack.slotValues.get(2).matched).intValue()));
        labelMap.put("date(ymdString,m,d)",
                valuePack -> DateUtils.ymdStrDate(valuePack.matched,
                        ((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("date(m,d,y)",
                valuePack -> DateUtils.ymdDate(((Double) valuePack.slotValues.get(2).matched).intValue(),
                        ((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("date(d,m,y)",
                valuePack -> DateUtils.ymdDate(((Double) valuePack.slotValues.get(2).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue(),
                        ((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("thisDay()",
                valuePack -> DateUtils.thisDay());
        labelMap.put("prevDay(1)",
                valuePack -> DateUtils.prevDayOne());
        labelMap.put("prevDay(2)",
                valuePack -> DateUtils.prevDayTwo());
        labelMap.put("prevDay(3)",
                valuePack -> DateUtils.prevDayThree());
        labelMap.put("nextDay(1)",
                valuePack -> DateUtils.nextDayOne());
        labelMap.put("nextDay(2)",
                valuePack -> DateUtils.nextDayTwo());
        labelMap.put("nextDay(3)",
                valuePack -> DateUtils.nextDayThree());
        labelMap.put("thisSunday()",
                valuePack -> DateUtils.thisSunday());
        labelMap.put("prevSunday()",
                valuePack -> DateUtils.prevSunday());
        labelMap.put("nextSunday()",
                valuePack -> DateUtils.nextSunday());
        labelMap.put("thisWeekday(wd)",
                valuePack -> DateUtils.thisWeekday(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevWeekday(wd)",
                valuePack -> DateUtils.prevWeekday(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextWeekday(wd)",
                valuePack -> DateUtils.nextWeekday(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("thisWeek()",
                valuePack -> DateUtils.thisWeek());
        labelMap.put("prevWeek()",
                valuePack -> DateUtils.prevWeek());
        labelMap.put("nextWeek()",
                valuePack -> DateUtils.nextWeek());
        labelMap.put("thisTenDays()",
                valuePack -> DateUtils.thisTendays());
        labelMap.put("firstTenDays()",
                valuePack -> DateUtils.firstTendays());
        labelMap.put("midTenDays()",
                valuePack -> DateUtils.midTendays());
        labelMap.put("lastTenDays()",
                valuePack -> DateUtils.lastTendays());
        labelMap.put("thisMonth()",
                valuePack -> DateUtils.thisMonth());
        labelMap.put("prevMonth()",
                valuePack -> DateUtils.prevMonth());
        labelMap.put("nextMonth()",
                valuePack -> DateUtils.nextMonth());
        labelMap.put("prevMonthFirstTd()",
                valuePack -> DateUtils.prevMonthFirstTd());
        labelMap.put("nextMonthFirstTd()",
                valuePack -> DateUtils.nextMonthFirstTd());
        labelMap.put("prevMonthMidTd()",
                valuePack -> DateUtils.prevMonthMidTd());
        labelMap.put("nextMonthMidTd()",
                valuePack -> DateUtils.nextMonthMidTd());
        labelMap.put("prevMonthLastTd()",
                valuePack -> DateUtils.prevMonthLastTd());
        labelMap.put("nextMonthLastTd()",
                valuePack -> DateUtils.nextMonthLastTd());
        labelMap.put("monthFirstTd(m)",
                valuePack -> DateUtils.monthFirstTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("monthMidTd(m)",
                valuePack -> DateUtils.monthMidTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("monthLastTd(m)",
                valuePack -> DateUtils.monthLastTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("thisMonthDay(d)",
                valuePack -> DateUtils.thisMonthDay(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevMonthDay(d)",
                valuePack -> DateUtils.prevMonthDay(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextMonthDay(d)",
                valuePack -> DateUtils.nextMonthDay(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("thisSeason()",
                valuePack -> DateUtils.thisSeason());
        labelMap.put("prevSeason()",
                valuePack -> DateUtils.prevSeason());
        labelMap.put("nextSeason()",
                valuePack -> DateUtils.nextSeason());
        labelMap.put("thisYear()",
                valuePack -> DateUtils.thisYear());
        labelMap.put("prevYear(1)",
                valuePack -> DateUtils.prevYearOne());
        labelMap.put("prevYear(2)",
                valuePack -> DateUtils.prevYearTwo());
        labelMap.put("prevYear(3)",
                valuePack -> DateUtils.prevYearThree());
        labelMap.put("prevYearThisSeason(1)",
                valuePack -> DateUtils.prevYearOneThisSeason());
        labelMap.put("prevYearThisSeason(2)",
                valuePack -> DateUtils.prevYearTwoThisSeason());
        labelMap.put("prevYearThisSeason(3)",
                valuePack -> DateUtils.prevYearThreeThisSeason());
        labelMap.put("prevYearPrevSeason(1)",
                valuePack -> DateUtils.prevYearOnePrevSeason());
        labelMap.put("prevYearPrevSeason(2)",
                valuePack -> DateUtils.prevYearTwoPrevSeason());
        labelMap.put("prevYearPrevSeason(3)",
                valuePack -> DateUtils.prevYearThreePrevSeason());
        labelMap.put("prevYearNextSeason(1)",
                valuePack -> DateUtils.prevYearOneNextSeason());
        labelMap.put("prevYearNextSeason(2)",
                valuePack -> DateUtils.prevYearTwoNextSeason());
        labelMap.put("prevYearNextSeason(3)",
                valuePack -> DateUtils.prevYearThreeNextSeason());
        labelMap.put("prevYearMonth(1,m)",
                valuePack -> DateUtils.prevYearOneMonth(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonth(2,m)",
                valuePack -> DateUtils.prevYearTwoMonth(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonth(3,m)",
                valuePack -> DateUtils.prevYearThreeMonth(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonthFirstTd(1,m)",
                valuePack -> DateUtils.prevYearOneMonthFirstTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonthFirstTd(2,m)",
                valuePack -> DateUtils.prevYearTwoMonthFirstTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonthFirstTd(3,m)",
                valuePack -> DateUtils.prevYearThreeMonthFirstTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonthMidTd(1,m)",
                valuePack -> DateUtils.prevYearOneMonthMidTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonthMidTd(2,m)",
                valuePack -> DateUtils.prevYearTwoMonthMidTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonthMidTd(3,m)",
                valuePack -> DateUtils.prevYearThreeMonthMidTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonthLastTd(1,m)",
                valuePack -> DateUtils.prevYearOneMonthLastTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonthLastTd(2,m)",
                valuePack -> DateUtils.prevYearTwoMonthLastTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonthLastTd(3,m)",
                valuePack -> DateUtils.prevYearThreeMonthLastTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("prevYearMonthDay(1,m,d)",
                valuePack -> DateUtils.prevYearOneMonthDay(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("prevYearMonthDay(2,m,d)",
                valuePack -> DateUtils.prevYearTwoMonthDay(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("prevYearMonthDay(3,m,d)",
                valuePack -> DateUtils.prevYearThreeMonthDay(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("prevYearThisDay(1)",
                valuePack -> DateUtils.prevYearOneThisDay());
        labelMap.put("prevYearThisDay(2)",
                valuePack -> DateUtils.prevYearTwoThisDay());
        labelMap.put("prevYearThisDay(3)",
                valuePack -> DateUtils.prevYearThreeThisDay());
        labelMap.put("nextYear(1)",
                valuePack -> DateUtils.nextYearOne());
        labelMap.put("nextYear(2)",
                valuePack -> DateUtils.nextYearTwo());
        labelMap.put("nextYear(3)",
                valuePack -> DateUtils.nextYearThree());
        labelMap.put("nextYearThisSeason(1)",
                valuePack -> DateUtils.nextYearOneThisSeason());
        labelMap.put("nextYearThisSeason(2)",
                valuePack -> DateUtils.nextYearTwoThisSeason());
        labelMap.put("nextYearThisSeason(3)",
                valuePack -> DateUtils.nextYearThreeThisSeason());
        labelMap.put("nextYearPrevSeason(1)",
                valuePack -> DateUtils.nextYearOnePrevSeason());
        labelMap.put("nextYearPrevSeason(2)",
                valuePack -> DateUtils.nextYearTwoPrevSeason());
        labelMap.put("nextYearPrevSeason(3)",
                valuePack -> DateUtils.nextYearThreePrevSeason());
        labelMap.put("nextYearNextSeason(1)",
                valuePack -> DateUtils.nextYearOneNextSeason());
        labelMap.put("nextYearNextSeason(2)",
                valuePack -> DateUtils.nextYearTwoNextSeason());
        labelMap.put("nextYearNextSeason(3)",
                valuePack -> DateUtils.nextYearThreeNextSeason());
        labelMap.put("nextYearMonth(1,m)",
                valuePack -> DateUtils.nextYearOneMonth(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonth(2,m)",
                valuePack -> DateUtils.nextYearTwoMonth(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonth(3,m)",
                valuePack -> DateUtils.nextYearThreeMonth(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonthFirstTd(1,m)",
                valuePack -> DateUtils.nextYearOneMonthFirstTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonthFirstTd(2,m)",
                valuePack -> DateUtils.nextYearTwoMonthFirstTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonthFirstTd(3,m)",
                valuePack -> DateUtils.nextYearThreeMonthFirstTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonthMidTd(1,m)",
                valuePack -> DateUtils.nextYearOneMonthMidTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonthMidTd(2,m)",
                valuePack -> DateUtils.nextYearTwoMonthMidTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonthMidTd(3,m)",
                valuePack -> DateUtils.nextYearThreeMonthMidTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonthLastTd(1,m)",
                valuePack -> DateUtils.nextYearOneMonthLastTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonthLastTd(2,m)",
                valuePack -> DateUtils.nextYearTwoMonthLastTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonthLastTd(3,m)",
                valuePack -> DateUtils.nextYearThreeMonthLastTd(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("nextYearMonthDay(1,m,d)",
                valuePack -> DateUtils.nextYearOneMonthDay(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("nextYearMonthDay(2,m,d)",
                valuePack -> DateUtils.nextYearTwoMonthDay(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("nextYearMonthDay(3,m,d)",
                valuePack -> DateUtils.nextYearThreeMonthDay(((Double) valuePack.slotValues.get(0).matched).intValue(),
                        ((Double) valuePack.slotValues.get(1).matched).intValue()));
        labelMap.put("nextYearThisDay(1)",
                valuePack -> DateUtils.nextYearOneThisDay());
        labelMap.put("nextYearThisDay(2)",
                valuePack -> DateUtils.nextYearTwoThisDay());
        labelMap.put("nextYearThisDay(3)",
                valuePack -> DateUtils.nextYearThreeThisDay());
        labelMap.put("thisCentury()",
                valuePack -> DateUtils.thisCentury());
        labelMap.put("prevCentury(1)",
                valuePack -> DateUtils.prevCentury());
        labelMap.put("nextCentury(1)",
                valuePack -> DateUtils.nextCentury());
        labelMap.put("beforeCentury(c)",
                valuePack -> DateUtils.beforeCentury(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeCenturyHalf(c)",
                valuePack -> DateUtils.beforeCenturyHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeCenturyHalf()",
                valuePack -> DateUtils.beforeCenturyHalf());
        labelMap.put("beforeYear(y)",
                valuePack -> DateUtils.beforeYear(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeYearHalf(y)",
                valuePack -> DateUtils.beforeYearHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeYearHalf()",
                valuePack -> DateUtils.beforeYearHalf());
        labelMap.put("beforeSeason(s)",
                valuePack -> DateUtils.beforeSeason(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeMonth(m)",
                valuePack -> DateUtils.beforeMonth(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeMonthHalf(m)",
                valuePack -> DateUtils.beforeMonthHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeMonthHalf()",
                valuePack -> DateUtils.beforeMonthHalf());
        labelMap.put("beforeTendays(td)",
                valuePack -> DateUtils.beforeTendays(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeWeek(w)",
                valuePack -> DateUtils.beforeWeek(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("beforeDay(d)",
                valuePack -> DateUtils.beforeDay(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterCentury(c)",
                valuePack -> DateUtils.afterCentury(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterCenturyHalf(c)",
                valuePack -> DateUtils.afterCenturyHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterCenturyHalf()",
                valuePack -> DateUtils.afterCenturyHalf());
        labelMap.put("afterYear(y)",
                valuePack -> DateUtils.afterYear(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterYearHalf(y)",
                valuePack -> DateUtils.afterYearHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterYearHalf()",
                valuePack -> DateUtils.afterYearHalf());
        labelMap.put("afterMonth(m)",
                valuePack -> DateUtils.afterMonth(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterMonthHalf(m)",
                valuePack -> DateUtils.afterMonthHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterMonthHalf()",
                valuePack -> DateUtils.afterMonthHalf());
        labelMap.put("afterTendays(td)",
                valuePack -> DateUtils.afterTendays(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterWeek(w)",
                valuePack -> DateUtils.afterWeek(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterDay(d)",
                valuePack -> DateUtils.afterDay(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterYear(y)",
                valuePack -> DateUtils.afterYear(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterYearHalf(y)",
                valuePack -> DateUtils.afterYearHalf(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterYearHalf()",
                valuePack -> DateUtils.afterYearHalf());
        labelMap.put("afterSeason(s)",
                valuePack -> DateUtils.afterSeason(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterTendays(td)",
                valuePack -> DateUtils.afterTendays(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterWeek(w)",
                valuePack -> DateUtils.afterWeek(((Double) valuePack.slotValues.get(0).matched).intValue()));
        labelMap.put("afterDay(d)",
                valuePack -> DateUtils.afterDay(((Double) valuePack.slotValues.get(0).matched).intValue()));


        /* get regex list */
        this.regexList = fetchRegexFile("date_regex_file");
    }

}
