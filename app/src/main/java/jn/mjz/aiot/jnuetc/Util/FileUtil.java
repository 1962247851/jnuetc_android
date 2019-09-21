package jn.mjz.aiot.jnuetc.Util;


import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import jn.mjz.aiot.jnuetc.Application.MyApplication;
import jn.mjz.aiot.jnuetc.Greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;

public class FileUtil {
    // Unique request code.
    public static final int WRITE_REQUEST_CODE = 43;

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

    public interface IOnExportListener {
        void OnSuccess();

        void OnError();
    }
}
