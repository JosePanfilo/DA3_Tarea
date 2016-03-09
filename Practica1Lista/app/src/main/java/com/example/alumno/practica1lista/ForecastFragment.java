package com.example.alumno.practica1lista;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    ArrayAdapter<String> arrAdapter;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vistaGenerada = inflater.inflate(R.layout.fragment_main, container, false);
        //Arreglo de informacion de pruebapara el listview
        ArrayList<String> informacionDePrueba  =new ArrayList<String>();
        informacionDePrueba.add("Domingo -- Soleado -- 33/22");
        informacionDePrueba.add("Lunes -- Nublado -- 33/22");
        informacionDePrueba.add("Martes -- Livioso -- 33/22");
        informacionDePrueba.add("Miercoles -- Tormentoso -- 33/22");
        informacionDePrueba.add("Jueves -- neblinoso -- 33/22");
        informacionDePrueba.add("Viernes -- Soleado -- 33/22");
        informacionDePrueba.add("Sabado -- Luvioso -- 33/22");
        informacionDePrueba.add("Domingo -- Tormntoso -- 33/22");
        informacionDePrueba.add("Lunes -- Huracanoso -- 33/22");
        informacionDePrueba.add("Martes -- Nublado -- 33/22");

        arrAdapter = new ArrayAdapter<String>(
                getActivity(), //Contexto
                R.layout.list_item_forecast,//id de list item
                R.id.list_item_forecast_textview,//id del textview del list item
                informacionDePrueba //datos de la lista
        );
        ListView lvForecastList = (ListView) vistaGenerada.findViewById(R.id.list_view_forecast);
        lvForecastList.setAdapter(arrAdapter);

        //Se instancia
        CargadorDePronostico cdp = new CargadorDePronostico();
        cdp.execute();//se ejecuta el pronostico asincrono como hilo


        return vistaGenerada;//inflater.inflate(R.layout.fragment_main, container, false);
    }
    //////////////////////////////////////////////////////////////////////////////////////////
    private class CargadorDePronostico extends AsyncTask<Void, Void, String[]>{
        @Override
        protected String[] doInBackground(Void... params) {

            String[] datos = consultarAPI();

            return datos;
        }

        @Override
        protected void onPostExecute(String[] dias) {
            super.onPostExecute(dias);

            List<String> listaDatos = Arrays.asList(dias);
            arrAdapter.clear();
            arrAdapter.addAll(listaDatos);

        }

        protected String[] consultarAPI(){
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String [] arregloHuesped = new String[0];

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=83553,MX&mode=json&units=metric&cnt=7";
                //String apiKey = "&APPID=0e5293f87768ca5c02c14068458369bd" ;//+ BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                URL url = new URL("http://hoteltel.ticcode.net/Huesped/JsonIndex");//.concat(apiKey));

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                //el JSON en forma de un String muy largo
                forecastJsonStr = buffer.toString();

                //Necesito Parsear el JSON para obtener un arreglo de String
                //donde se vea la inoformacion de cada dia
                ExtractorDeDatosJson v = new ExtractorDeDatosJson();

                try {
                    //arreglo = v.getWeatherDataFromJson(forecastJsonStr);//, 7);
                    arregloHuesped = v.getHuespedDataFromJson(forecastJsonStr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.v("CargadorDePronostico","El JSON Recibido fue: "+forecastJsonStr);

               /* try {
                    ExtractorDeDatosJson.getMaxTemp(forecastJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            return arregloHuesped;
        }
    }

}

class ExtractorDeDatosJson {
    /*Nos arroja la temperatura maxima de un dia segun su index
    a partir de una cadena de caracteres que representa el JSON recibido
    del API de pronosticos
     */
   /* public static String getCityName(String strJSON)
            throws JSONException {

        String cityName= new JSONObject(strJSON).
                getJSONObject("city").
                getString("name");

        return cityName;
    }*/

    public String[] getHuespedDataFromJson(String strJSON) throws JSONException {
        JSONArray arrHuesped = new JSONArray(strJSON);

        String[] strHuesped = new String[arrHuesped.length()];
        for (int i = 0; i <arrHuesped.length() ; i++) {
            int huespedID;
            String nombre;
            String apellidoP;
            String apellidoM;
            String telefono;
            JSONObject jsonHuesped = arrHuesped.getJSONObject(i);
            huespedID = jsonHuesped.getInt("huespedID");
            nombre = jsonHuesped.getString("nombre");
            apellidoP = jsonHuesped.getString("apellidoP");
            apellidoM = jsonHuesped.getString("apellidoM");
            telefono = jsonHuesped.getString("telefono");

            strHuesped[i] = huespedID + " - " + nombre + " - " + apellidoP + " - " + apellidoM + "  - " + telefono;


        }

        return strHuesped;
    }
    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    /*public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        //String[] datosHuesped = {};
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[0];
        for(int i = 0; i < forecastJson.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            //description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            Log.v("getWeatherDataFromJson", "Forecast entry: " + s);
        }
        return resultStrs;

    }


    public static void getMaxTemp(String strJSON)
            throws JSONException {

        JSONObject jso = new JSONObject(strJSON);
        JSONArray jsArregloDias = jso.getJSONArray("list");

        for (int i = 0; i < jsArregloDias.length(); i++) {
            double maxTemp = jsArregloDias.getJSONObject(i).
                    getJSONObject("temp").
                    getDouble("max");

            Log.d("ExtractorDeDatosJson", "Maxima del dia "+(i+1)+": "+maxTemp);
        }
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
    /*private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
   /* private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }*/

}