package org.docksidestage.dockside.unit;

import org.dbflute.bhv.BehaviorSelector;
import org.dbflute.bhv.BehaviorWritable;
import org.dbflute.bhv.writable.DeleteOption;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.utflute.spring.ContainerTestCase;
import org.docksidestage.dockside.JdbcBeansJavaConfig;
import org.docksidestage.dockside.dbflute.allcommon.DBFluteBeansJavaConfig;
import org.docksidestage.dockside.dbflute.exbhv.MemberAddressBhv;
import org.docksidestage.dockside.dbflute.exbhv.MemberFollowingBhv;
import org.docksidestage.dockside.dbflute.exbhv.MemberLoginBhv;
import org.docksidestage.dockside.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.dockside.dbflute.exbhv.MemberServiceBhv;
import org.docksidestage.dockside.dbflute.exbhv.MemberWithdrawalBhv;
import org.docksidestage.dockside.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.dockside.dbflute.exbhv.PurchasePaymentBhv;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * The test case with container for unit test.
 * @author jflute
 * @since 0.6.3 (2008/02/02 Saturday)
 */
public abstract class UnitContainerTestCase extends ContainerTestCase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private BehaviorSelector _behaviorSelector;

    // ===================================================================================
    //                                                                             Prepare
    //                                                                             =======
    @Override
    protected ApplicationContext provideDefaultApplicationContext() {
        return new AnnotationConfigApplicationContext(JdbcBeansJavaConfig.class, DBFluteBeansJavaConfig.class);
    }

    // ===================================================================================
    //                                                                         Data Helper
    //                                                                         ===========
    protected void deleteAll(Class<? extends BehaviorWritable> clazz) {
        BehaviorWritable bhv = _behaviorSelector.select(clazz);
        ConditionBean cb = bhv.newConditionBean();
        bhv.rangeRemove(cb, new DeleteOption<ConditionBean>().allowNonQueryDelete());
    }

    protected void deleteMemberReferrer() {
        deleteAll(MemberAddressBhv.class);
        deleteAll(MemberFollowingBhv.class);
        deleteAll(MemberLoginBhv.class);
        deleteAll(MemberServiceBhv.class);
        deleteAll(MemberSecurityBhv.class);
        deleteAll(MemberWithdrawalBhv.class);
        deleteAll(PurchasePaymentBhv.class);
        deleteAll(PurchaseBhv.class);
    }
}
