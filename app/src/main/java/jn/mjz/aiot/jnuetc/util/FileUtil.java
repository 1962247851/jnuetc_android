package jn.mjz.aiot.jnuetc.util;


import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.youth.xframe.utils.http.HttpCallBack;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jn.mjz.aiot.jnuetc.application.MyApplication;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;
import okhttp3.Response;

public class FileUtil {
    // Unique request code.
    public static final int WRITE_REQUEST_CODE = 43;
    public static final int READ_REQUEST_CODE = 44;
    public static final int PIC_PICTURE = 45;
    private static final String TAG = "FileUtil";

    public static void ExportDatasToExcel(AppCompatActivity activity, Uri uri, IOnExportListener onExportListener) {
        // 设置第一行名
        String[] title = {"报修序号", "报修时间", "状态", "报修人", "学院", "年级", "电话", "QQ", "园区", "南北区", "设备型号", "问题详情", "维修人", "接单时间", "维修时间", "对用户电脑水平评估", "服务内容", "故障描述及解决过程"};
        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet sheet = workbook.createSheet();

        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = null;

        // 写入第一行
        for (int i = 0; i < title.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
        }
        DataDao dataDao = MyApplication.getDaoSession().getDataDao();
        List<Data> dataList = dataDao.loadAll();

        //写入数据
        for (int i = 1; i <= dataList.size(); i++) {

            HSSFRow nextrow = sheet.createRow(i);
            Data data = dataList.get(i - 1);
            HSSFCell cell2 = nextrow.createCell(0);
            cell2.setCellValue(data.getId());

            cell2 = nextrow.createCell(1);
            cell2.setCellValue(DateUtil.getDateAndTime(data.getDate(), " "));

            cell2 = nextrow.createCell(2);
            cell2.setCellValue(data.getState() == 0 ? "未处理" : data.getState() == 1 ? "已接单" : "已维修");

            cell2 = nextrow.createCell(3);
            cell2.setCellValue(data.getName());

            cell2 = nextrow.createCell(4);
            cell2.setCellValue(data.getCollege());

            cell2 = nextrow.createCell(5);
            cell2.setCellValue(data.getGrade());

            cell2 = nextrow.createCell(6);
            cell2.setCellValue(data.getTel());

            cell2 = nextrow.createCell(7);
            cell2.setCellValue(data.getQq());

            cell2 = nextrow.createCell(8);
            cell2.setCellValue(data.getLocal());

            cell2 = nextrow.createCell(9);
            cell2.setCellValue(data.getDistrict() == 0 ? "北区" : "南区");

            cell2 = nextrow.createCell(10);
            cell2.setCellValue(data.getModel());

            cell2 = nextrow.createCell(11);
            cell2.setCellValue(data.getMessage());

            cell2 = nextrow.createCell(12);
            cell2.setCellValue(data.getRepairer());

            cell2 = nextrow.createCell(13);
            cell2.setCellValue(data.getState() != 0 ? DateUtil.getDateAndTime(data.getOrderDate(), " ") : "");

            cell2 = nextrow.createCell(14);
            cell2.setCellValue(data.getState() == 2 ? DateUtil.getDateAndTime(data.getRepairDate(), " ") : "");

            cell2 = nextrow.createCell(15);
            cell2.setCellValue(data.getMark());

            cell2 = nextrow.createCell(16);
            cell2.setCellValue(data.getService());

            cell2 = nextrow.createCell(17);
            cell2.setCellValue(data.getRepairMessage());

        }

        try {
            FileOutputStream fos = getFOSFromUri(activity, uri);
            workbook.write(fos);
            fos.close();
            onExportListener.OnSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            onExportListener.OnError();
        }
    }

    public static void UploadTipDp(String tipDpName, File tipDp, HttpCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("tipDpName", tipDpName);
        params.put("sno", MainViewModel.user.getSno());
        params.put("tipDp", tipDp);
        Log.e(TAG, "UploadTipDp: "+tipDp.length() );
        HttpUtil.post.uploadHaveResponse(GlobalUtil.URLS.FILE.UPLOAD_TIP_DP, params, new HttpUtil.HttpUtilCallBack() {
            @Override
            public void onResponse(Response response, Object result) {
                Log.e(TAG, "onResponse: " + result);
            }

            @Override
            public void onFailure(IOException e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

    public static void createFile(AppCompatActivity activity, String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        activity.startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public static void selectFile(AppCompatActivity activity, String mimeType) {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType(mimeType);

        activity.startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public static FileOutputStream getFOSFromUri(AppCompatActivity activity, Uri uri) {
        ParcelFileDescriptor pfd = null;
        try {
            pfd = activity.getContentResolver().
                    openFileDescriptor(uri, "w");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new FileOutputStream(pfd.getFileDescriptor());
    }

    //跳转相册
    public static void toPicture(AppCompatActivity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK);  //跳转到 ACTION_IMAGE_CAPTURE
        intent.setType("image/*");
        activity.startActivityForResult(intent, PIC_PICTURE);
    }

    public interface IOnExportListener {
        void OnSuccess();

        void OnError();
    }
}
