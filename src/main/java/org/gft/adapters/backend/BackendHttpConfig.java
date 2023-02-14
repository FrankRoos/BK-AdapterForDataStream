package org.gft.adapters.backend;


import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class BackendHttpConfig {

    private final Integer length;
    private final String username;
    private final String password;
    private final String signal_name;
    private String lowest_date;
    private final String highest_date;
    private String filter;

    private final JsonObject pindos_signals = new Gson().fromJson("{\"PINDOS Signal HW_FM_Flow - FD\":\"618d4fade191ea48057c1a8d\", \"PINDOS Signal HW_FM_Tot - FD\":\"618d4fcd68387a67545e67e3\", " +
            "\"PINDOS Signal Ptot - Dr1\":\"618d3d155c2d32157b434782\", \"PINDOS Signal Ptot - Dr2\":\"6189572ca15d1114f912d093\", \"PINDOS Signal Ptot - Dr3\":\"618d3fbf7ec30a4cf2014a75\", " +
            "\"PINDOS Signal Ptot - Ph\":\"618d415e54ef02535004a25a\", \"PINDOS Signal Ptot - Ww1\":\"618d43633f6027149c2c0f5f\", \"PINDOS Signal Ptot - Ww2\":\"618d4507536f70692e5839be\", " +
            "\"PINDOS Signal SB_FM_Flow - FD\":\"618d4b40ba7f19144a228d90\", \"PINDOS Signal SB_FM_Tot - FD\":\"618d4f4e536f70692e5839f2\", \"PINDOS Signal SH_FM_Flow - FD\":\"618d4ff7ba7f19144a228d9f\", " +
            "\"PINDOS Signal SH_FM_Tot - FD\":\"618d51aaa73af145294f138f\"}", JsonObject.class);
    private final JsonObject astander_signals = new Gson().fromJson("{\"Altivar fault code\":\"6167f85c151693290874fd32\", \"Drive state\":\"6167f85c151693290874fd33\"}", JsonObject.class);
    private final JsonObject nodes_id = new Gson().fromJson("{\"PINDOS\":\"61855a064f181d0f3a3b4d42\",\"ASTANDER\":\"6167f8078870124d6f1bc5e2\"}", JsonObject.class);
    private boolean first_time = true;
    DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BackendHttpConfig(String username, String password, String signal_name, String lowest_date, String highest_date, Integer length) {
        this.username = username;
        this.password = password;
        this.signal_name = signal_name;
        this.lowest_date = lowest_date;
        this.highest_date = highest_date;
        this.length = length;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public Integer getLength() {
        return this.length;
    }

    public String getClientId() {
        return "1";
    }

    public String getClientSecret(){
        return "oPKFIotoK1GiccRxQWWeFcXo4TbWq8fEhDFl0TJs";
    }

    public String getPage(){
        return "1";
    }

    public String getLoginUrl(){
        return "https://kyklos-backend.kyklos40project.eu:1121/oauth/token";
    }

    public String getBaseUrl(){
        return "https://kyklos-backend.kyklos40project.eu:1121/api/v0.1/kyklos_core_signal_readings/view/records";
    }

    public String getSort(){
        return "[{\"scope\":\"date\",\"value\":\"asc\"}]";
    }

    public String getGrantType(){
        return "password";
    }
    public String getHighestDate(){
        return highest_date;
    }
    public String getLowestDate(){
        return lowest_date;
    }

    public String getScope(){
        return "read_scheduler_administrator write_scheduler_administrator read_dashboards_administrator write_dashboards_administrator " +
                    "read_datasources_administrator write_datasources_administrator read_raw_signals_administrator write_raw_signals_administrator " +
                    "read_raw_signal_readings_administrator write_raw_signal_readings_administrator read_nodes_administrator write_nodes_administrator read_components_administrator " +
                    "write_components_administrator read_signal_readings_administrator read_conversions_administrator write_conversions_administrator read_data_fusion_administrator " +
                    "write_data_fusion_administrator read_user_relationships_administrator read_users_administrator read_profile_administrator read_events_signal_administrator " +
                    "read_events_data_source_administrator read_datasinks_administrator write_datasinks_administrator read_dashboards_basic_user write_dashboards_basic_user " +
                    "delete_dashboards_basic_user read_nodes_basic_user write_nodes_basic_user delete_nodes_basic_user share_nodes_basic_user read_components_basic_user " +
                    "write_components_basic_user delete_components_basic_user read_component_signals_basic_user write_component_signals_basic_user delete_component_signals_basic_user " +
                    "read_component_alerts_basic_user write_component_alerts_basic_user delete_component_alerts_basic_user read_signal_readings_basic_user " +
                    "write_signal_readings_basic_user delete_signal_readings_basic_user read_profile_new_user";
    }

    public String getFilter(String lowest_date, String highest_date) {
        if(this.highest_date.equals("CurrentDateTime")){
            if(this.pindos_signals.has(this.signal_name)){
                this.filter = "[{\"scope\":\"comp_signal.node._id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":["+this.nodes_id.get("PINDOS")+"]}," +
                        "{\"scope\":\"comp_signal_id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":["+ this.pindos_signals.get(this.signal_name)+"]}," +
                        "{\"scope\":\"date\",\"type\":\"date-range\",\"operator\":\">= <\",\"value\":\"" + lowest_date +" - "+ highest_date +"\"}]";
            } else if (this.astander_signals.has(this.signal_name)) {
                this.filter = "[{\"scope\":\"comp_signal.node._id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":["+this.nodes_id.get("ASTANDER")+"]}," +
                        "{\"scope\":\"comp_signal_id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":["+ this.astander_signals.get(this.signal_name)+"]}," +
                        "{\"scope\":\"date\",\"type\":\"date-range\",\"operator\":\">= <\",\"value\":\"" + lowest_date +" - "+ highest_date +"\"}]";
            }
        }else{
            if(this.pindos_signals.has(this.signal_name)){
                this.filter = "[{\"scope\":\"comp_signal.node._id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":["+this.nodes_id.get("PINDOS")+"]}," +
                        "{\"scope\":\"comp_signal_id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":["+ this.pindos_signals.get(this.signal_name)+"]}," +
                        "{\"scope\":\"date\",\"type\":\"date-range\",\"operator\":\">= <\",\"value\":\"" + lowest_date +" - "+ this.highest_date +"\"}]";
            } else if (this.astander_signals.has(this.signal_name)) {
                this.filter = "[{\"scope\":\"comp_signal.node._id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":["+this.nodes_id.get("ASTANDER")+"]}," +
                        "{\"scope\":\"comp_signal_id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":["+ this.astander_signals.get(this.signal_name)+"]}," +
                        "{\"scope\":\"date\",\"type\":\"date-range\",\"operator\":\">= <\",\"value\":\"" + lowest_date +" - "+ this.highest_date +"\"}]";
            }
        }
        return this.filter;
    }

    public String CurrentDateTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public String NextDateTime(long offset){
        Date myDate = null;

        try{
            myDate = date_format.parse(this.lowest_date);
        }catch (ParseException e){
            e.printStackTrace();
        }

        assert myDate != null;
        LocalDateTime local_date_time = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        local_date_time = local_date_time.plusMinutes(offset);

        Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());

        return date_format.format(date_plus);
    }

    public String LastDateTime(long offset) {
        Date myDate = null;

        try{
            myDate = date_format.parse(this.lowest_date);
        }catch (ParseException e){
            e.printStackTrace();
        }

        assert myDate != null;
        LocalDateTime local_date_time = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if(this.first_time){
            this.first_time = false;
            local_date_time = local_date_time.minusMinutes(offset);
        }
        local_date_time = local_date_time.plusMinutes(offset);

        Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());
        String date = date_format.format(date_plus);
        this.lowest_date = date;

        return date;
    }

    String getMillis(String date){
        String timestamp = null;
        try{
            Date myDate = date_format.parse(date);
            timestamp = String.valueOf(myDate.getTime());
        }catch (ParseException e){
            e.printStackTrace();
        }
        return timestamp;
    }

}
