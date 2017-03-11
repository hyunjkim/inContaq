package nyc.c4q.jonathancolon.inContaq.contactlist.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

//import com.db.chart.view.LineChartView;

//import org.parceler.Parcels;

import com.db.chart.view.LineChartView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import nyc.c4q.jonathancolon.inContaq.R;
import nyc.c4q.jonathancolon.inContaq.contactlist.Contact;
import nyc.c4q.jonathancolon.inContaq.contactlist.activities.ContactListActivity;
import nyc.c4q.jonathancolon.inContaq.notifications.ContactNotification;
import nyc.c4q.jonathancolon.inContaq.utlities.graphs.MonthlyGraph;
import nyc.c4q.jonathancolon.inContaq.utlities.sms.Sms;
import nyc.c4q.jonathancolon.inContaq.utlities.sms.SmsHelper;


public class ContactStatsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private LineChartView lineGraph;
    private Spinner dateSpinner;
    private TextView frequentWord;
    private ArrayAdapter<CharSequence> spinnerArrayAdapter;
    private ContactNotification mContactNotification;

    public static ContactStatsFragment newInstance() {
        ContactStatsFragment fragment = new ContactStatsFragment();
        Bundle b = new Bundle();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_stats, container, false);

        initViews(view);
        Contact contact = unwrapParcelledContact();
        ArrayList<Sms> lstSms = SmsHelper.getAllSms(getActivity(), contact);

        MonthlyGraph monthlyGraph = new MonthlyGraph(getContext(), lineGraph, lstSms);
        monthlyGraph.showMonthlyGraph();
        return view;
    }

    private void initViews(View view) {
        lineGraph = (LineChartView) view.findViewById(R.id.daily_chart);
        dateSpinner = (Spinner) view.findViewById(R.id.date_spinner);
        frequentWord = (TextView) view.findViewById(R.id.freq_word_text);
        spinnerArrayAdapter = ArrayAdapter.createFromResource(
                view.getContext(),
                R.array.date_spinner_array,
                R.layout.date_spinner_item);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.date_spinner_dropdown_item);
        dateSpinner.setAdapter(spinnerArrayAdapter);
        dateSpinner.setOnItemSelectedListener(this);
    }

    @Nullable
    private Contact unwrapParcelledContact() {
        return Parcels.unwrap(getActivity().getIntent().getParcelableExtra(ContactListActivity.PARCELLED_CONTACT));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Contact contact = unwrapParcelledContact();
        switch (String.valueOf(parent.getItemAtPosition(position))) {
            case "WEEKLY":
                // TODO: 3/8/17 if last sent text == to 7 days + last sent text date then, notification
                mContactNotification = new ContactNotification();
                mContactNotification.startNotification(getContext(), contact);
                break;
            case "2 WEEKS":
                // TODO: 3/8/17 if last sent text == to 14 days + last sent text date then, notification
                break;
            case "3 WEEKS":
                // TODO: 3/8/17 if last sent text == to 21 days + last sent text date then, notification
                break;
            case "MONTHLY":
                // TODO: 3/8/17 if last sent text == to 30 days + last sent text date then, notification
                break;
        }
    }

    //Method used to find the most frequent words used in conversation between user and contact
//    public void Map<Sms, Integer> getWordFrequencies(List<Sms> words) {
//        ArrayList<Sms> lstSms = (ArrayList<Sms>) SmsHelper.getAllSms(getActivity(), contact);
//
//        Map<Sms, Long> map = lstSms.stream()
//                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
//
//        List<Map.Entry<Sms, Long>> result = map.entrySet().stream()
//                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
//                .limit(10)
//                .collect(Collectors.toList());
//    }


//
//public static Map<String, Integer> getWordFrequencies(List<Sms> words) {
//    Map<Sms, Long> counts =
//            Stream.of(WALKING, WALKING, JOGGING, JOGGING, STANDING)
//                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
//
//    long max = Collections.max(counts.values());
//    List<Sms> result = counts
//            .entrySet()
//            .stream()
//            .filter(e -> e.getValue().longValue() == max)
//            .map(Entry::getKey)
//            .collect(Collectors.toList());
//
//}
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}


