package jn.mjz.aiot.jnuetc.util;

import com.youth.xframe.XFrame;

import jn.mjz.aiot.jnuetc.R;

/**
 * @author 19622
 */
public class GlobalUtil {

    private static final String HOST = "https://www.jiangnan-dzjsb.club:444/";
    public static final String[] TITLES_N = {"杏园", "桃园", "桔园", "桂园", "梅园", "榴园", "李园", "竹园"};
    public static final String[] TITLES_S = {"澈苑", "溪苑", "清苑", "涓苑", "润苑", "浩苑", "瀚苑", "鸿苑"};
    public static final String[] MARKS = XFrame.getResources().getStringArray(R.array.spinner_mark_entries);
    public static final String[] SERVICES = XFrame.getResources().getStringArray(R.array.spinner_service_entries);
    public static final String[] GRADES = XFrame.getResources().getStringArray(R.array.spinner_grade_entries);
    public static final String[] COLLEGES = XFrame.getResources().getStringArray(R.array.spinner_college_entries);
    public static final String[] LOCATIONS = XFrame.getResources().getStringArray(R.array.spinner_local_entries);

    public static final class Keys {

        public static final class LoginActivity {
            public static final String FILE_NAME = "login_info";
            public static final String REMEMBER_PASSWORD = "0";
            public static final String AUTO_LOGIN = "1";
            public static final String USER_JSON_STRING = "2";
        }

        public static final class New {
            public static final String FILE_NAME_DRAWER = "drawerSettings1";
        }

        public static final class Processing {
            public static final String FILE_NAME_DRAWER = "drawerSettings2";
        }

        public static final class Done {
            public static final String FILE_NAME_DRAWER = "drawerSettings3";
        }

    }

    public static final class Urls {

        public static final class Code {
            private static final String CODE = HOST + "code/";

            public static final String INSERT = CODE + "insert";
        }

        public static final class Version {
            private static final String VERSION = HOST + "version/";

            public static final String QUERY_ALL = VERSION + "queryAll";
        }

        public static final class User {
            private static final String USER = HOST + "user/";

            public static final String LOGIN = USER + "login";
            public static final String QUERY_ALL = USER + "queryAll";
            public static final String MODIFY = USER + "modify";
        }

        public static final class Data {
            private static final String DATA = HOST + "data/";

            public static final String QUERY_ALL = DATA + "queryAll";
            public static final String QUERY_BY_ID = DATA + "queryById";
            public static final String UPDATE = DATA + "update";
            public static final String INSERT = DATA + "insert";
            public static final String DELETE_BY_ID_LIST = DATA + "deleteByIdList";
        }

        public static final class State {
            private static final String STATE = HOST + "state/";
            public static final String CHECK_SERVICE = STATE + "checkService";
            public static final String CHANGE_SERVICE = STATE + "changeService";
        }

        public static final class MingJu {
            private static final String MING_JU = HOST + "mingju/";
            public static final String GET_MING_JU = MING_JU + "getMingJu";
        }

        public static final class File {
            private static final String FILE = HOST + "file/";

            public static final String DOWNLOAD = FILE + "download";
            public static final String UPLOAD = FILE + "upload";
        }

    }

}

