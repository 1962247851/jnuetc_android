package jn.mjz.aiot.jnuetc.Greendao.Dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;

import jn.mjz.aiot.jnuetc.Greendao.Dao.DataDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig dataDaoConfig;

    private final DataDao dataDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        dataDaoConfig = daoConfigMap.get(DataDao.class).clone();
        dataDaoConfig.initIdentityScope(type);

        dataDao = new DataDao(dataDaoConfig, this);

        registerDao(Data.class, dataDao);
    }
    
    public void clear() {
        dataDaoConfig.clearIdentityScope();
    }

    public DataDao getDataDao() {
        return dataDao;
    }

}
