package kkkb1114.sampleproject.infectionapp.API;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import kkkb1114.sampleproject.infectionapp.pill.PillAdapter;

public class OpenApi extends AsyncTask<Void, Void, String> {

    String targetName;
    RecyclerView rv_pill;
    ArrayList<String> ad;
    Context context;
    ArrayList<String> af;
    String key = "X2YEdMzvbE3wTlxyaDcpFy8pnSAanX7AJVVnVBwtExvyfZGkS2%2B7UBgwBO9Dx131egaZE50OFpE926apkhTVWA%3D%3D";
    PillAdapter pillAdapter;

    ArrayList data;
    public OpenApi(String targetName, ArrayList<String> ad,ArrayList<String> af,PillAdapter pillAdapter,Context context,RecyclerView rv_pill) {
        this.targetName = targetName;
        this.ad=ad;
        this.af=af;
        this.pillAdapter=pillAdapter;
        this.rv_pill=rv_pill;
        this.context=context;
    }

    @Override
    protected String doInBackground(Void... params) {
        data=new ArrayList();
        // parsing할 url 지정(API 키 포함해서)
        URL url = null;
        try {
            String encodeStr = URLEncoder.encode(targetName, "UTF-8");
            url = new URL("https://apis.data.go.kr/1471000/DrugPrdtPrmsnInfoService03/getDrugPrdtMcpnDtlInq02?serviceKey="+ key + "&pageNo=" + 1 + "&numOfRows=100&Prduct=" + encodeStr);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Log.d("url", String.valueOf(url));
            Log.d("target",targetName);
            DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            try {
                dBuilder = dbFactoty.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            try {
                doc = dBuilder.parse(new InputSource(url.openStream()));
            } catch (IOException | SAXException e) {
                e.printStackTrace();
            }

            // root tag
            doc.getDocumentElement().normalize();
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName()); // Root element: result

            // 파싱할 tag
            NodeList nList = doc.getElementsByTagName("item");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;



                    if(!ad.contains(getTagValue("PRDUCT", eElement))) {
                        ad.add(getTagValue("PRDUCT", eElement));
                        af.add(getTagValue("MAIN_INGR_ENG", eElement));
                    }

                }    // for end
            }

            Handler handler = new Handler(Looper.getMainLooper());

            handler.post(new Runnable() {

            @Override
            public void run() {
                pillAdapter.notifyDataSetChanged();
            }

        });

            // if end

        return null;
    }

    @Override
    protected void onPostExecute(String str) {
        super.onPostExecute(str);
    }


    private String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if (nValue == null)
            return null;
        return nValue.getNodeValue();
    }

    public void setRecyclerView(View view, String date){




    }
}