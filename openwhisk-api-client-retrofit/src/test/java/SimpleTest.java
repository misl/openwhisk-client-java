
import java.util.List;

import nl.xup.openwhisk.Openwhisk;
import nl.xup.openwhisk.api.model.gson.EntityBrief;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SimpleTest {

  public static void main( String[] args ) {
    final String hostUrl = "https://openwhisk.ng.bluemix.net:443";
    final String authName = "YOUR-AUTHNAME";
    final String apiKey = "YOUR-APIKEY";
    final String namespace = "YOUR-NAMESPACE";
    final Openwhisk openwhisk = new Openwhisk( hostUrl, authName, apiKey, namespace);
    Call<List<EntityBrief>> response = openwhisk.packages().getAlPackages( namespace, null, null, null);
    response.enqueue( new Callback<List<EntityBrief>>() {
      
      @Override
      public void onResponse( Call<List<EntityBrief>> call, Response<List<EntityBrief>> response ) {
        System.out.println( "packages:" );
        for (EntityBrief entityBrief : response.body()) {
          System.out.println( entityBrief.getName() );
        }
      }
      
      @Override
      public void onFailure( Call<List<EntityBrief>> call, Throwable t ) {
        t.printStackTrace();
      }
    } );
  }
}
