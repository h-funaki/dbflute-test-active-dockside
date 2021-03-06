package org.docksidestage.dockside.dbflute.whitebox.allcommon;

import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.exception.IllegalDBFluteConfigAccessException;
import org.dbflute.exception.InvalidQueryRegisteredException;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.util.Srl;
import org.docksidestage.dockside.dbflute.allcommon.DBFluteConfig;
import org.docksidestage.dockside.dbflute.cbean.MemberCB;
import org.docksidestage.dockside.dbflute.exbhv.MemberBhv;
import org.docksidestage.dockside.dbflute.exbhv.pmbean.SimpleMemberPmb;
import org.docksidestage.dockside.dbflute.exentity.Member;
import org.docksidestage.dockside.unit.UnitContainerTestCase;

/**
 * @author jflute
 */
public class WxDBFluteConfigTest extends UnitContainerTestCase {

    protected MemberBhv memberBhv;

    // ===================================================================================
    //                                                                              Set up
    //                                                                              ======
    @Override
    public void setUp() throws Exception {
        DBFluteConfig.getInstance().unlock();
        final StatementConfig config = new StatementConfig();
        DBFluteConfig.getInstance().setEmptyStringQueryAllowed(true);
        DBFluteConfig.getInstance().setEmptyStringParameterAllowed(true);
        DBFluteConfig.getInstance().setNullOrEmptyQueryAllowed(true);
        config.typeForwardOnly().queryTimeout(10).fetchSize(7).maxRows(3);
        DBFluteConfig.getInstance().setDefaultStatementConfig(config);
        DBFluteConfig.getInstance().setInternalDebug(true);

        // normally you don't need to lock
        // because the initializer of DBFlute locks when initializing
        // but a container instance is recycled in this project's test cases
        // so it locks here (for a test case special reason)
        DBFluteConfig.getInstance().lock();
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        DBFluteConfig.getInstance().unlock();
        DBFluteConfig.getInstance().setEmptyStringQueryAllowed(false);
        DBFluteConfig.getInstance().setEmptyStringParameterAllowed(false);
        DBFluteConfig.getInstance().setNullOrEmptyQueryAllowed(false);
        DBFluteConfig.getInstance().setDefaultStatementConfig(null);
        DBFluteConfig.getInstance().setInternalDebug(false);
        DBFluteConfig.getInstance().lock();
    }

    // ===================================================================================
    //                                                                       Invalid Query
    //                                                                       =============
    public void test_invalidQuery_emptyStringQueryAllowed_basic() throws Exception {
        // ## Arrange ##
        MemberCB cb = new MemberCB();
        cb.query().setMemberName_Equal(""); // expects no exception

        // ## Act ##
        // ## Assert ##
        assertTrue(Srl.contains(cb.toDisplaySql(), "MEMBER_NAME = ''"));
        cb.disableEmptyStringQuery(); // expects no exception
        cb.query().setMemberAccount_Equal(""); // expects no exception
        cb.enableOverridingQuery(() -> {
            cb.query().setMemberName_Equal(""); // expects no exception
            cb.query().setMemberAccount_Equal(""); // expects no exception
        });
        try {
            cb.enableOverridingQuery(() -> {
                cb.checkNullOrEmptyQuery();
                cb.query().setMemberAccount_Equal("");
            });
            fail();
        } catch (InvalidQueryRegisteredException e) {
            log(e.getMessage());
        }
    }

    public void test_invalidQuery_emptyStringParameterAllowed_basic() throws Exception {
        // ## Arrange ##
        SimpleMemberPmb pmb = new SimpleMemberPmb();

        // ## Act ##
        pmb.setMemberName_PrefixSearch("");
        String memberName = pmb.getMemberName();

        // ## Assert ##
        assertEquals("", memberName);
    }

    public void test_invalidQuery_invalidQueryChecked_basic() throws Exception {
        // ## Arrange ##
        MemberCB cb = new MemberCB();
        cb.query().setMemberName_Equal(null);
        cb.checkNullOrEmptyQuery();
        try {
            cb.query().setMemberName_Equal(null);

            // ## Assert ##
            fail();
        } catch (InvalidQueryRegisteredException e) {
            // OK
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                     StatementConfig
    //                                                                     ===============
    public void test_ConditionBean_configure_Config_is_Request() throws Exception {
        // ## Arrange ##
        final ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
            /* ## Act ## */
            cb.configure(conf -> conf.typeScrollSensitive().fetchSize(123).maxRows(1));
            pushCB(cb);
        });

        // ## Assert ##
        assertEquals(1, memberList.size()); // should be overridden
    }

    public void test_StatementConfig_check_insert_update_delete() throws Exception {
        // ## Arrange ##
        Member member = new Member();
        member.setMemberName("testName");
        member.setMemberAccount("testAccount");
        member.setMemberStatusCode_Formalized();

        // ## Act & Assert ##
        // expects no exception
        memberBhv.insert(member);
        memberBhv.updateNonstrict(member);
        memberBhv.deleteNonstrict(member);
    }

    // ===================================================================================
    //                                                                          Log Format
    //                                                                          ==========
    public void test_LogDateFormat_basic() {
        // ## Arrange ##
        MemberCB cb = new MemberCB();
        cb.query().setBirthdate_GreaterEqual(toLocalDate("2008/06/15 12:34:56.123"));
        String beforeSql = cb.toDisplaySql();
        log(beforeSql);
        assertTrue(beforeSql.contains("'2008-06-15'"));
        try {
            DBFluteConfig.getInstance().unlock();
            DBFluteConfig.getInstance().setLogDatePattern("yyyy$MM$dd");
            // ## Act & Assert ##
            String sql = cb.toDisplaySql();
            log(sql);
            assertTrue("sql:" + ln() + sql, sql.contains("'2008$06$15'"));
        } finally {
            DBFluteConfig.getInstance().setLogDatePattern(null);
            DBFluteConfig.getInstance().lock();
        }
    }

    public void test_LogDateFormat_prefixSuffix() {
        // ## Arrange ##
        MemberCB cb = new MemberCB();
        cb.query().setBirthdate_GreaterEqual(toLocalDate("2008/06/15 12:34:56.123"));
        String beforeSql = cb.toDisplaySql();
        log(beforeSql);
        assertTrue(beforeSql.contains("'2008-06-15'"));
        try {
            DBFluteConfig.getInstance().unlock();
            DBFluteConfig.getInstance().setLogDatePattern("date $df:{yyyy=MM=dd}");
            // ## Act & Assert ##
            String sql = cb.toDisplaySql();
            log(sql);
            assertTrue("sql:\n" + sql, sql.contains("date '2008=06=15'"));
        } finally {
            DBFluteConfig.getInstance().setLogDatePattern(null);
            DBFluteConfig.getInstance().lock();
        }
    }

    public void test_LogTimestampFormat_basic() {
        // ## Arrange ##
        MemberCB cb = new MemberCB();
        cb.query().setRegisterDatetime_GreaterEqual(toLocalDateTime("2008/06/15 12:34:56.123"));
        String beforeSql = cb.toDisplaySql();
        log(beforeSql);
        assertTrue(beforeSql.contains("'2008-06-15 12:34:56.123'"));
        try {
            DBFluteConfig.getInstance().unlock();
            DBFluteConfig.getInstance().setLogTimestampPattern("yyyy/MM/dd HH-mm-ss.SSS");
            // ## Act & Assert ##
            String sql = cb.toDisplaySql();
            log(sql);
            assertTrue(sql.contains("'2008/06/15 12-34-56.123'"));
        } finally {
            DBFluteConfig.getInstance().setLogTimestampPattern(null);
            DBFluteConfig.getInstance().lock();
        }
    }

    public void test_LogTimestampFormat_prefixSuffix() {
        // ## Arrange ##
        MemberCB cb = new MemberCB();
        cb.query().setRegisterDatetime_GreaterEqual(toLocalDateTime("2008/06/15 12:34:56.123"));
        String beforeSql = cb.toDisplaySql();
        log(beforeSql);
        assertTrue(beforeSql.contains("'2008-06-15 12:34:56.123'"));
        try {
            DBFluteConfig.getInstance().unlock();
            DBFluteConfig.getInstance().setLogTimestampPattern("timestamp $df:{yyyy/MM/dd HH-mm-ss.SSS}");
            // ## Act & Assert ##
            String sql = cb.toDisplaySql();
            log(sql);
            assertTrue(sql.contains("timestamp '2008/06/15 12-34-56.123'"));
        } finally {
            DBFluteConfig.getInstance().setLogTimestampPattern(null);
            DBFluteConfig.getInstance().lock();
        }
    }

    // ===================================================================================
    //                                                                                Lock
    //                                                                                ====
    public void test_always_locked() {
        assertTrue(DBFluteConfig.getInstance().isLocked());
    }

    public void test_locked_setting() {
        // ## Arrange & Act ##
        try {
            DBFluteConfig.getInstance().setLogDatePattern(null);

            // ## Assert ##
            fail();
        } catch (IllegalDBFluteConfigAccessException e) {
            // OK
            log(e.getMessage());
        }
    }
}
