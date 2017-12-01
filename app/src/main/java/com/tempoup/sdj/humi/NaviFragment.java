package com.tempoup.sdj.humi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.app.Fragment;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.IInterface;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

// 권한 요청 코드



        /* Provider 자동 선택 (부정확)
        Criteria crt = new Criteria();
        crt.setAccuracy(Criteria.ACCURACY_FINE); //정확도
        crt.setPowerRequirement(Criteria.POWER_MEDIUM); //베터리 소모량
        crt.setAltitudeRequired(true); //고도
        crt.setCostAllowed(true);
        Criteria criteria = new Criteria();

        String mProvider = locationManager.getBestProvider(crt, true);
        */




public class NaviFragment extends Fragment implements LocationListener {
    //Bluetooth connection
    static final int REQUEST_ENABLE_BT = 10;
    static final int D_LEDSTATE_LEFT = 1;
    static final int D_LEDSTATE_RIGHT = 2;
    static final int D_LEDSTATE_EMER = 3;

    private Realm m_Realm;
    private int m_mPairedDeviceCount;
    private Set<BluetoothDevice> m_Devices;
    private BluetoothAdapter m_BluetoothAdapter;
    private BluetoothDevice m_RemoteDeivce;
    private BluetoothSocket m_Socket;
    private InputStream m_InputStream;
    private String m_StrDelimiter;
    private char m_CharDelimiter;
    private Thread m_WorkerThread;
    private byte[] m_ReadBuffer;
    private int m_nReadBufferPosition;


    private LocationManager m_locationMgr;
    private TMapView m_naviTmapView;
    private EditText m_destPtEdit;
    private ListView m_poiResultListView;
    private TextView m_speedText;
    private TextView m_disText;
    private TextView m_timeText;
    private TextView m_avgText;
    private FloatingActionButton m_trackingFAB;
    private ImageView m_ledImageViewer;

    private TMapView m_tMapView;
    private TMapGpsManager m_tMapGPS;
    private String m_sMapAPIkey;
    private TMapPoint m_startGPSPt;
    private TMapPoint m_destGPSPt;

    private String m_timeLeft;
    private float m_avgSpeed;

    private ArrayAdapter<String> m_poiResultAdapter;
    private ArrayList<String> m_poiResultListItem;
    private ArrayList<TMapPoint> m_poiResultPoint;
    private HashMap m_poiResultItems;

    private int m_nSelectedInd;

    private ArrayList<TMapPoint> m_pathPointDataList;
    private ArrayList<String> m_pathDirDataList;

    private int m_nPivotPathPoint;

    private boolean m_bIsInPoint;

    public NaviFragment() {
        m_Realm = null;
        m_mPairedDeviceCount = 0;

        m_locationMgr = null;
        m_naviTmapView = null;
        m_destPtEdit = null;
        m_poiResultListView = null;
        m_speedText = null;
        m_timeText = null;
        m_avgText = null;
        m_trackingFAB = null;
        m_ledImageViewer = null;

        m_tMapView = null;
        m_tMapGPS = null;
        m_sMapAPIkey = "c9384c45-4332-3a90-89b3-fe6f3483b93f";
        m_startGPSPt = null;
        m_destGPSPt = null;

        m_timeLeft = new String();
        m_avgSpeed = 0.0f;

        m_poiResultAdapter = null;
        m_poiResultListItem = null;
        m_poiResultPoint = null;

        m_nSelectedInd = -1;

        m_pathPointDataList = null;
        m_pathDirDataList = null;

        m_nPivotPathPoint = 0;
        m_bIsInPoint = false;
        // Required empty public constructor
    }
    public void btConnection(){
        checkBluetooth();
    }
    void checkBluetooth() {
        /**
         * getDefaultAdapter() : 만일 폰에 블루투스 모듈이 없으면 null 을 리턴한다.
         이경우 Toast를 사용해 에러메시지를 표시하고 앱을 종료한다.
         */
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(m_BluetoothAdapter == null ) {  // 블루투스 미지원
            Toast.makeText(getActivity(), "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            getActivity().finish();  // 앱종료
        }
        else { // 블루투스 지원
            /** isEnable() : 블루투스 모듈이 활성화 되었는지 확인.
             *               true : 지원 ,  false : 미지원
             */
            if(!m_BluetoothAdapter.isEnabled()) { // 블루투스 지원하며 비활성 상태인 경우.
                Toast.makeText(getActivity(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // REQUEST_ENABLE_BT : 블루투스 활성 상태의 변경 결과를 App 으로 알려줄 때 식별자로 사용(0이상)
                /**
                 startActivityForResult 함수 호출후 다이얼로그가 나타남
                 "예" 를 선택하면 시스템의 블루투스 장치를 활성화 시키고
                 "아니오" 를 선택하면 비활성화 상태를 유지 한다.
                 선택 결과는 onActivityResult 콜백 함수에서 확인할 수 있다.
                 */
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else // 블루투스 지원하며 활성 상태인 경우.
                selectDevice();
        }
    }

    // 블루투스 지원하며 활성 상태인 경우.
    void selectDevice() {
        // 블루투스 디바이스는 연결해서 사용하기 전에 먼저 페어링 되어야만 한다
        // getBondedDevices() : 페어링된 장치 목록 얻어오는 함수.
        m_Devices = m_BluetoothAdapter.getBondedDevices();
        m_mPairedDeviceCount = m_Devices.size();

        if(m_mPairedDeviceCount == 0 ) { // 페어링된 장치가 없는 경우.
            Toast.makeText(getActivity(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            return;
            //getActivity().finish(); // App 종료.
        }
        // 페어링된 장치가 있는 경우.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("블루투스 장치 선택");

        // 각 디바이스는 이름과(서로 다른) 주소를 가진다. 페어링 된 디바이스들을 표시한다.
        List<String> listItems = new ArrayList<String>();
        for(BluetoothDevice device : m_Devices) {
            // device.getName() : 단말기의 Bluetooth Adapter 이름을 반환.
            listItems.add(device.getName());
        }
        listItems.add("취소");  // 취소 항목 추가.


        // CharSequence : 변경 가능한 문자열.
        // toArray : List형태로 넘어온것 배열로 바꿔서 처리하기 위한 toArray() 함수.
        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        // toArray 함수를 이용해서 size만큼 배열이 생성 되었다.
        listItems.toArray(new CharSequence[listItems.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                // TODO Auto-generated method stub
                if(item == m_mPairedDeviceCount) { // 연결할 장치를 선택하지 않고 '취소' 를 누른 경우.
                    Toast.makeText(getActivity(), "연결할 장치를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
                else { // 연결할 장치를 선택한 경우, 선택한 장치와 연결을 시도함.
                    connectToSelectedDevice(items[item].toString());
                }
            }

        });

        //builder.setCancelable(false);  // 뒤로 가기 버튼 사용 금지.
        //AlertDialog alert = builder.create();
        //alert.show();
    }

    //  connectToSelectedDevice() : 원격 장치와 연결하는 과정을 나타냄.
    //        실제 데이터 송수신을 위해서는 소켓으로부터 입출력 스트림을 얻고 입출력 스트림을 이용하여 이루어 진다.
    void connectToSelectedDevice(String selectedDeviceName) {
        // BluetoothDevice 원격 블루투스 기기를 나타냄.
        m_RemoteDeivce = getDeviceFromBondedList(selectedDeviceName);
        // java.util.UUID.fromString : 자바에서 중복되지 않는 Unique 키 생성.
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // 소켓 생성, RFCOMM 채널을 통한 연결.
            // createRfcommSocketToServiceRecord(uuid) : 이 함수를 사용하여 원격 블루투스 장치와 통신할 수 있는 소켓을 생성함.
            // 이 메소드가 성공하면 스마트폰과 페어링 된 디바이스간 통신 채널에 대응하는 BluetoothSocket 오브젝트를 리턴함.
            m_Socket = m_RemoteDeivce.createRfcommSocketToServiceRecord(uuid);
            m_Socket.connect(); // 소켓이 생성 되면 connect() 함수를 호출함으로써 두기기의 연결은 완료된다.

            // 데이터 송수신을 위한 스트림 얻기.
            // BluetoothSocket 오브젝트는 두개의 Stream을 제공한다.
            // 1. 데이터를 보내기 위한 OutputStrem
            // 2. 데이터를 받기 위한 InputStream
            m_InputStream = m_Socket.getInputStream();

            // 데이터 수신 준비.
            btConnection_receive();

        }catch(Exception e) { // 블루투스 연결 중 오류 발생
            Toast.makeText(getActivity(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            getActivity().finish();  // App 종료
        }
    }

    // 블루투스 장치의 이름이 주어졌을때 해당 블루투스 장치 객체를 페어링 된 장치 목록에서 찾아내는 코드.
    BluetoothDevice getDeviceFromBondedList(String name) {
        // BluetoothDevice : 페어링 된 기기 목록을 얻어옴.
        BluetoothDevice selectedDevice = null;
        // getBondedDevices 함수가 반환하는 페어링 된 기기 목록은 Set 형식이며,
        // Set 형식에서는 n 번째 원소를 얻어오는 방법이 없으므로 주어진 이름과 비교해서 찾는다.
        for(BluetoothDevice deivce : m_Devices) {
            // getName() : 단말기의 Bluetooth Adapter 이름을 반환
            if(name.equals(deivce.getName())) {
                selectedDevice = deivce;
                break;
            }
        }
        return selectedDevice;
    }


    public void btConnection_receive()
    {
        final Handler handler = new Handler();

        m_nReadBufferPosition = 0;                 // 버퍼 내 수신 문자 저장 위치.
        m_ReadBuffer = new byte[1024];            // 수신 버퍼.

        // 문자열 수신 쓰레드.
        m_WorkerThread = new Thread(new Runnable()
        {
            @Override
            public void run() {
                // interrupt() 메소드를 이용 스레드를 종료시키는 예제이다.
                // interrupt() 메소드는 하던 일을 멈추는 메소드이다.
                // isInterrupted() 메소드를 사용하여 멈추었을 경우 반복문을 나가서 스레드가 종료하게 된다.
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        // InputStream.available() : 다른 스레드에서 blocking 하기 전까지 읽은 수 있는 문자열 개수를 반환함.
                        int byteAvailable = m_InputStream.available();   // 수신 데이터 확인
                        if(byteAvailable > 0) {                        // 데이터가 수신된 경우.
                            byte[] packetBytes = new byte[byteAvailable];
                            // read(buf[]) : 입력스트림에서 buf[] 크기만큼 읽어서 저장 없을 경우에 -1 리턴.
                            m_InputStream.read(packetBytes);
                            for(int i=0; i<byteAvailable; i++) {
                                byte b = packetBytes[i];
                                if(b == m_CharDelimiter) {
                                    byte[] encodedBytes = new byte[m_nReadBufferPosition];
                                    //  System.arraycopy(복사할 배열, 복사시작점, 복사된 배열, 붙이기 시작점, 복사할 개수)
                                    //  readBuffer 배열을 처음 부터 끝까지 encodedBytes 배열로 복사.
                                    System.arraycopy(m_ReadBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    final String data = new String(encodedBytes, "US-ASCII");
                                    m_nReadBufferPosition = 0;

                                    handler.post(new Runnable(){
                                        // 수신된 문자열 데이터에 대한 처리.
                                        @Override
                                        public void run() {
                                            // mStrDelimiter = '\n';
                                            String signal = data+m_StrDelimiter;
                                            setChangeVestLED(Integer.parseInt(signal));

                                            validationVestSignal(m_bIsInPoint, signal);
                                            Toast.makeText(getActivity(), signal, Toast.LENGTH_LONG).show();
                                            //setChangeVestLED();
                                            //mEditReceive.setText(mEditReceive.getText().toString() + data+ mStrDelimiter);
                                        }

                                    });
                                }
                                else {
                                    m_ReadBuffer[m_nReadBufferPosition++] = b;
                                }
                            }
                        }

                    } catch (Exception e) {    // 데이터 수신 중 오류 발생.
                        Toast.makeText(getActivity(), "데이터 수신 중 오류가 발생 했습니다.", Toast.LENGTH_LONG).show();
                        getActivity().finish();            // App 종료.
                    }
                }
            }

        });
    }

    public void TMapInit() {
        m_tMapView = new TMapView(getActivity());
        m_tMapView.setSKPMapApiKey(m_sMapAPIkey); //발급받은 api 키
        m_tMapView.setIconVisibility(true);
        m_tMapView.setZoomLevel(15);
        m_tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        m_tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        m_tMapView.setCompassMode(true);
        m_tMapView.setSightVisible(true);
        //m_tMapView.setUserScrollZoomEnable(true);
        //m_tMapView.setSlideMode(true);
        //m_tMapView.setEnableClustering(true);


        //m_tMapView.setMapType(TMapView.MAPTYPE_SATELLITE);

        //m_tMapView.setZoomLevel(5);

        m_tMapGPS = new TMapGpsManager(getActivity());
        m_tMapGPS.setMinTime(1000);
        m_tMapGPS.setMinDistance(5);

        //와이파이 및 네트워크
        m_tMapGPS.setProvider(m_tMapGPS.NETWORK_PROVIDER);

        //위성
        //m_tMapGPS.setProvider(m_tMapGPS.GPS_PROVIDER);

        m_tMapGPS.OpenGps();

        //m_tMapView.setTrackingMode(true);

    }

    public TMapView getTmapView() {
        return m_tMapView;
    }

    public void findPathDataDetail(TMapPoint ptStart, TMapPoint ptEnd)
    {
        TMapData tmapdata = new TMapData();

        m_pathPointDataList.clear();
        m_pathDirDataList.clear();
        m_nPivotPathPoint = 0;

        tmapdata.findPathDataAllType(TMapData.TMapPathType.CAR_PATH, ptStart, ptEnd, new TMapData.FindPathDataAllListenerCallback() {
            @Override
            public void onFindPathDataAll(Document document) {
                Element root = document.getDocumentElement();
                NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");
                for( int i=0; i<nodeListPlacemark.getLength(); i++ ) {
                    NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();
                    for( int j=0; j<nodeListPlacemarkItem.getLength(); j++ ) {
                        if( nodeListPlacemarkItem.item(j).getNodeName().equals("description") ) {
                            Log.d("debug", nodeListPlacemarkItem.item(j).getTextContent().trim() );
                        }
//                        if( nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:nodeType") )
//                        {
//                            Log.d("debug", "nodeType!" );
//                            if( nodeListPlacemarkItem.item(j).getTextContent().trim().equals("POINT") ){
//                                IsbPointType = true;
//                                Log.d("debug", "nodeType : POINT!" );
//                            }
//                            else{
//                                IsbPointType = false;
//                                Log.d("debug", "nodeType : NOT POINT!" );
//                            }
//                        }
                        if( nodeListPlacemarkItem.item(j).getNodeName().equals("Point") ){
                            String[] coord;
                            coord = nodeListPlacemarkItem.item(j).getTextContent().trim().split(",");
                            TMapPoint point = new TMapPoint(Double.parseDouble(coord[1]), Double.parseDouble(coord[0]));
                            m_pathPointDataList.add(point);

                        }

                        if( nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:turnType") ){
                            String str = nodeListPlacemarkItem.item(j).getTextContent().trim();
                            m_pathDirDataList.add(str);
                        }
                    }
                }
            }
        });

    }

    public void drawMapPath(double lat, double log) {

        TMapPoint point1 = m_tMapView.getCenterPoint();
        TMapPoint point2 = new TMapPoint(lat, log);

        TMapData tmapdata = new TMapData();

        tmapdata.findPathData(point1, point2, new TMapData.FindPathDataListenerCallback() {

            public void onFindPathData(TMapPolyLine polyLine) {
                m_tMapView.addTMapPath(polyLine);
            }
        });

        findPathDataDetail(point1, point2);

        //Toast.makeText(getActivity(), "drawMapPath!", Toast.LENGTH_SHORT).show();
    }

    public void findAllPoi() {
        m_poiResultListItem.clear();
        //m_poiResultItems.clear();
        m_poiResultPoint.clear();

        TMapData tmapdata = new TMapData();

        tmapdata.findAllPOI(m_destPtEdit.getText().toString(), new TMapData.FindAllPOIListenerCallback() {

            public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                for (int i = 0; i < poiItem.size(); i++) {
                    TMapPOIItem item = poiItem.get(i);

                    m_poiResultListItem.add(item.getPOIName().toString());

                    m_poiResultPoint.add(item.getPOIPoint());
                    //m_poiResultItems.put(item.getPOIName().toString(), new TMapPoint(m_dDstLat, m_dDstLog));
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        try{
//                            final Object lock = new Object();
//                            synchronized (lock) { lock.wait(500);}
//                        }
//                        catch (InterruptedException e){
//                            e.printStackTrace();
//                        }

                        m_poiResultAdapter.notifyDataSetChanged();
                    }
                });

            }
        });
    }

    public void HideListview() {
        m_poiResultListItem.clear();

        m_tMapView.setVisibility(View.VISIBLE);
        m_poiResultListView.setVisibility(View.INVISIBLE);
        m_trackingFAB.setVisibility(View.VISIBLE);
    }

    public void setChangeVestLED(int ledState)
    {
        switch(ledState){
            case D_LEDSTATE_LEFT:
                m_ledImageViewer.setImageResource(R.mipmap.l);
                break;

            case D_LEDSTATE_RIGHT:
                m_ledImageViewer.setImageResource(R.mipmap.r);
                break;

            case D_LEDSTATE_EMER:
                m_ledImageViewer.setImageResource(R.mipmap.e);
                break;

            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        m_destPtEdit.setText("");
        HideListview();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //권한 확인 후 요청
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("Check", "NoPermisson");
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }


        View view = inflater.inflate(R.layout.fragment_navi, container, false);

//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
//                    Toast.makeText(getActivity(), "you just touch the screen :-)", Toast.LENGTH_SHORT).show();
//                }
//                return true;
//            }
//        });

        //레아아웃 아이디 연결
        LinearLayout NaviLinearLayout = (LinearLayout) view.findViewById(R.id.NaviTopLinearLayout);
        m_destPtEdit = (EditText) view.findViewById(R.id.destLocation);
        Button searchBtn = (Button) view.findViewById(R.id.searchPath);
        m_poiResultListView = (ListView) view.findViewById(R.id.searchResultList);
        FrameLayout NaviFrameLayout = (FrameLayout) view.findViewById(R.id.naviFrameLayout);
        m_trackingFAB = (FloatingActionButton) view.findViewById(R.id.TrackingFAB);
        m_ledImageViewer = (ImageView) view.findViewById(R.id.ledImageViewer);
        m_speedText = (TextView) view.findViewById(R.id.speedValue);
        m_disText = (TextView) view.findViewById(R.id.disValue);
        m_timeText = (TextView) view.findViewById(R.id.timeValue);
        m_avgText = (TextView) view.findViewById(R.id.avgValue);
        m_ledImageViewer.setImageResource(R.mipmap.e);

        m_poiResultListItem = new ArrayList<String>();
        m_poiResultAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                m_poiResultListItem);
        m_poiResultListView.setAdapter(m_poiResultAdapter);

        TMapInit();

        //AVD test code
        m_tMapView.setCenterPoint(127.073139, 37.550260);
        m_tMapView.setLocationPoint(127.073139, 37.550260);

        //m_naviTmapView = Singleton.getInstance().getTmapView();
        //m_naviTmapView.invalidate();

        m_locationMgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        m_locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                100,
                5, this);

        NaviFrameLayout.addView(m_tMapView);

        m_poiResultPoint = new ArrayList<TMapPoint>();
        m_startGPSPt = new TMapPoint(0, 0);
        m_destGPSPt = new TMapPoint(0, 0);

        m_pathPointDataList = new ArrayList<TMapPoint>();
        m_pathDirDataList = new ArrayList<String>();

        //RealmConfiguration config = new RealmConfiguration.Builder(getActivity()).build();

        m_Realm = Singleton.getInstance().getRealm();


        Singleton.getInstance().setInitialize(System.currentTimeMillis());

        //btConnection();


        //트래킹 버튼 터치
        m_trackingFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (m_tMapView.getIsTracking() == true) {
                    m_tMapView.setTrackingMode(false);
                } else {
                    m_tMapView.setTrackingMode(true);
                    m_tMapView.setCompassMode(true);
                }

            }
        });

        //출발 버튼 터치
        searchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                m_Realm.beginTransaction();

                RealmQuery<User> query = m_Realm.where(User.class);
                RealmResults<User> result = query.findAll();
                User user = result.first();
                user.addExp(10);

                m_Realm.commitTransaction();

                if (m_nSelectedInd == -1) {
                    return;
                }


                TMapPoint point = (TMapPoint) m_poiResultPoint.get(m_nSelectedInd);

                drawMapPath(point.getLatitude(), point.getLongitude());


            }
        });

        //리스트뷰 아이템 선택
        m_poiResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                view.setSelected(true);

                TextView c = (TextView) view;

                m_nSelectedInd = position;
                m_destPtEdit.setText(c.getText().toString());

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(m_destPtEdit.getWindowToken(), 0);

                HideListview();

                m_tMapView.setCompassMode(true);
                TMapPoint point = (TMapPoint) m_poiResultPoint.get(m_nSelectedInd);
                m_destGPSPt.setLatitude(point.getLatitude());
                m_destGPSPt.setLongitude(point.getLongitude());
                drawMapPath(point.getLatitude(), point.getLongitude());

            }
        });
        //도착지 텍스트 입력 변화
        m_destPtEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //입력되는 텍스트에 변화가 있을 때
//                Singleton.getInstance().getTmapView().setVisibility(View.INVISIBLE);
//                m_poiResultListView.setVisibility(View.VISIBLE);
//                m_poiResultAdapter.notifyDataSetChanged();
//                m_bIspoiUpdate = true;
//
//                findAllPoi();
                /*
                new Thread(){
                    public void run(){
                        Message message = handler.obtainMessage();
                        handler.sendMessage(message);
                    }
                }.start();*/

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //입력이 끝났을 때
                //Toast.makeText(getActivity(), "onTextChanged!", Toast.LENGTH_SHORT).show();
                if (m_destPtEdit.getText().toString().equals(String.valueOf(""))) {
                    //Toast.makeText(getActivity(), "block!", Toast.LENGTH_SHORT).show();
                    return;
                }

                m_tMapView.setVisibility(View.INVISIBLE);
                m_poiResultListView.setVisibility(View.VISIBLE);
                m_trackingFAB.setVisibility(View.INVISIBLE);
                m_poiResultAdapter.notifyDataSetChanged();

                findAllPoi();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //입력하기 전에
//                Singleton.getInstance().getTmapView().setVisibility(View.INVISIBLE);
//                m_poiResultListView.setVisibility(View.VISIBLE);
//                m_poiResultAdapter.notifyDataSetChanged();
//                m_bIspoiUpdate = true;
//
//                findAllPoi();
            }
        });


        return view;
    }

    public void getTickCountTimeLeft() {

        int hour = Singleton.getInstance().getHour();
        int min = Singleton.getInstance().getMin();
        m_timeLeft = "";

        if (hour < 10) {
            m_timeLeft += "0";
        }
        m_timeLeft += hour;
        m_timeLeft += ":";

        if (min < 10) {
            m_timeLeft += "0";
        }
        m_timeLeft += min;

        if(m_speedText == null){
            return;
        }


        Singleton.getInstance().addTotalSpeed(Float.parseFloat(m_speedText.getText().toString()));
        m_avgSpeed = Singleton.getInstance().getavgpeed();


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_timeText.setText(m_timeLeft);
                m_avgText.setText(String.valueOf(m_avgSpeed));
            }
        });

    }

    public void validationVestSignal(boolean bIsInPoint, String sBtSignal){
        if( m_bIsInPoint ){
            //좌회전 해야하는 상황에서 좌회전을 켰음
            if( sBtSignal.equals(String.valueOf(D_LEDSTATE_LEFT)) && m_pathDirDataList.get(m_nPivotPathPoint).equals("12") ){

            }
            //우회전 해야하는 상황에서 좌회전을 켰음
            if( sBtSignal.equals(String.valueOf(D_LEDSTATE_RIGHT)) && m_pathDirDataList.get(m_nPivotPathPoint).equals("13") ){

            }
        }
    }

    public void checkCurPoint()
    {
        if(m_pathPointDataList.size()-1 <= m_nPivotPathPoint){
            m_bIsInPoint = false;
            return;
        }
        TMapPoint curPoint = m_tMapView.getCenterPoint();
        TMapPoint endPoint = m_pathPointDataList.get(m_nPivotPathPoint);
        TMapPoint endNextPoint = m_pathPointDataList.get(m_nPivotPathPoint+1);

        float[] distance1 = new float[2];
        Location.distanceBetween(curPoint.getLatitude(), curPoint.getLongitude(), endPoint.getLatitude(), endPoint.getLongitude(), distance1);
        float[] distance2 = new float[2];
        Location.distanceBetween(curPoint.getLatitude(), curPoint.getLongitude(), endPoint.getLatitude(), endPoint.getLongitude(), distance2);



        //15m밖이면 조끼 버튼 체크 false
        if( distance1[0] > 15 ){
            m_bIsInPoint = false;
        }
        //15m 안이면 조끼 버튼 체크 true
        else{
            m_bIsInPoint = true;
        }
        if( distance2[0] < 15 ){
            m_nPivotPathPoint++;
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(getActivity(), "onLocationChanged!", Toast.LENGTH_SHORT).show();

        m_tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        m_tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());


        final Location startLoc = new Location(location);
        final Location destLoc = new Location(location);

        if (m_startGPSPt.getLatitude() == 0 && m_startGPSPt.getLongitude() == 0) {

            startLoc.setLatitude(location.getLatitude());
            startLoc.setLongitude(location.getLongitude());
        }
        destLoc.setLatitude(location.getLatitude());
        destLoc.setLongitude(location.getLongitude());

        m_speedText.setText(String.valueOf(location.getSpeed(       )));
        Log.d("debug", "Speed" + String.valueOf(location.getSpeed()));
        m_disText.setText(String.valueOf(startLoc.distanceTo(destLoc)));

        checkCurPoint();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
