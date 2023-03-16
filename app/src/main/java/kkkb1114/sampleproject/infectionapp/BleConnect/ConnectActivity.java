package kkkb1114.sampleproject.infectionapp.BleConnect;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.tools.PermissionManager;

public class ConnectActivity extends AppCompatActivity implements View.OnClickListener {

    public Toolbar toolbar_connect;
    ImageView iv_back;
    LinearLayout ln_item;
    TextView tv_name;
    TextView tv_address;
    TextView tv_connectState;
    RecyclerView rv_ble_list;
    BLEConnect_ListAdapter bleConnect_listAdapter;
    Context context;
    // 블루투스
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private ArrayList<BluetoothDevice> scanBleDeviceList;
    private ArrayList<String> scanBleDeviceAddressList; // 블루투스 기기 목록 중복 체크를 위한 ArrayList
    private ArrayList<ScanFilter> scanFilters;
    private ScanSettings setting;
    // 권한 체크
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        context = this;
        initView();
        setToolbar();
        setRecyclerView();
        setBluetoothBLE();
        checkMyBLE();
    }

    public void initView() {
        toolbar_connect = findViewById(R.id.toolbar_ble_connect);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        rv_ble_list = findViewById(R.id.rv_ble_list);
        ln_item = findViewById(R.id.ln_item);
        ln_item.setOnClickListener(this);
        tv_name = findViewById(R.id.tv_name);
        tv_address = findViewById(R.id.tv_address);
        tv_connectState = findViewById(R.id.tv_connectState);

        scanBleDeviceList = new ArrayList<>();
        scanBleDeviceAddressList = new ArrayList<>();
        permissionManager = new PermissionManager();
    }

    /** 연결한 BLE 기기 있는지 확인 (아직 작업중) **/
    public void checkMyBLE(){
        tv_name.setText("연결 되어있는 기기가 없습니다.");
    }

    /** 블루투스 세팅 **/
    public void setBluetoothBLE() {
        // bluetoothManager, bluetoothAdapter 세팅
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 블루투스가 지원되지 않는 경우 bluetoothAdapter가 null이 뜬다.
        if (bluetoothAdapter == null) {
        } else {
            // 블루투스가 꺼져있는지 확인
            if (bluetoothAdapter.isEnabled()) {
                // 스캐너 생성
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                // 스캔 필터
                scanFilters = new ArrayList<>();
                // 스캔 설정
                setting = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .build();
                // 스캔 콜백 메소드
                setBLEScanCallback();
                // 스캔 시작
                setBleScanTime();
            } else {
                Toast.makeText(context, "블루투스를 켜주신 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** 블루투스 스캔 콜백 **/
    public void setBLEScanCallback(){
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                checkBlePermission();
                processResult(result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                checkBlePermission();
                for (ScanResult result : results){
                    processResult(result);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);

            }

            private void processResult(final ScanResult result){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkBlePermission();
                        if (result != null){
                            if (result.getDevice().getName() != null){
                                // 중복 확인 (리스트에 같은 주소값이 있으면 add하지 않는다.)
                                if (!scanBleDeviceAddressList.contains(result.getDevice().getAddress())){
                                    scanBleDeviceList.add(result.getDevice());
                                    scanBleDeviceAddressList.add(result.getDevice().getAddress());
                                    bleConnect_listAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
            }
        };
    }

    /** RecyclerView 세팅 **/
    public void setRecyclerView(){
        bleConnect_listAdapter = new BLEConnect_ListAdapter(context, scanBleDeviceList, tv_name, tv_address, tv_connectState);
        rv_ble_list.setLayoutManager(new LinearLayoutManager(context));
        rv_ble_list.setAdapter(bleConnect_listAdapter);
        bleConnect_listAdapter.notifyDataSetChanged();
    }

    /** 스캔 시작, 정지 **/
    public void setBleScanTime(){
        checkBlePermission();
        //bluetoothLeScanner.startScan(scanFilters, setting, scanCallback);
        if (bluetoothLeScanner != null && scanCallback != null){
            bluetoothLeScanner.startScan(scanCallback);
        }

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                checkBlePermission();
                bluetoothLeScanner.stopScan(scanCallback);
            }
        };
        timer.schedule(timerTask, 10000);
    }

    /** 블루투스 주변 기기 검색 권한 체크 **/
    public void checkBlePermission(){
        // 권한 체크 (만약 권한이 허용되어있지 않으면 권한 요청)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (!(permissionManager.permissionCheck(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    permissionManager.permissionCheck(context, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    permissionManager.permissionCheck(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION))){

                permissionManager.requestPermission(context, 0, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                });
            }else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    if (!(permissionManager.permissionCheck(context, Manifest.permission.BLUETOOTH_CONNECT) ||
                            permissionManager.permissionCheck(context, Manifest.permission.BLUETOOTH_SCAN) ||
                            permissionManager.permissionCheck(context, Manifest.permission.BLUETOOTH))){

                        permissionManager.requestPermission(context, 0, new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH
                        });
                    }
                }
            }
        }
    }

    /** 툴바 세팅 **/
    public void setToolbar(){
        setSupportActionBar(toolbar_connect);
        getSupportActionBar().setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_ble_connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.search_again:
                // 블루투스 검색할때마다 목록 초기화를 위해 clear()
                scanBleDeviceList.clear();
                scanBleDeviceAddressList.clear();
                bleConnect_listAdapter.notifyDataSetChanged();
                // 스캔 시작
                setBleScanTime();
                break;
        }
        return true;
    }


    /** 권한 요청 결과 처리 메서드 **/
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionManager.PERMISSION_REQUEST_LOCATION_CODE:
            case PermissionManager.PERMISSION_REQUEST_CODE_LOCATION_S:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 허용되었을 때 실행할 코드 (위치 다음은 저장소 권한 요청)
                    startTestService();
                    checkStoragePermission();
                } else {
                    // 권한이 거부되었을 때 실행할 코드
                    Toast.makeText(this, "권한이 거부 되어있으면 기능을 이용하실 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case PermissionManager.PERMISSION_REQUEST_STORAGE_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 허용되었을 때 실행할 코드

                    startTestService();
                } else {
                    // 권한이 거부되었을 때 실행할 코드
                    Toast.makeText(this, "권한이 거부 되어있으면 기능을 이용하실 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }*/

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                finish();
                break;

            case R.id.ln_item:

                break;
        }
    }
}