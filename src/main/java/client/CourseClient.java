package client;

import com.in28minutes.courses.CourseDetails;
import com.in28minutes.courses.GetCourseDetailsRequest;
import com.in28minutes.courses.GetCourseDetailsResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;


public class CourseClient extends WebServiceGatewaySupport {

    public GetCourseDetailsResponse getCourseDetails(Integer id) {
        GetCourseDetailsRequest request = new GetCourseDetailsRequest();
        request.setId(id);

        System.out.println();
        System.out.println("Requesting course for " + id);

        GetCourseDetailsResponse response =
                (GetCourseDetailsResponse) getWebServiceTemplate()
                        .marshalSendAndReceive(request);

        return response;
    }

    public void printResponse(GetCourseDetailsResponse response) {
        CourseDetails courseDetails = response.getCourseDetails();

        System.out.println("Receiving information");
        System.out.println(courseDetails.getId());
        System.out.println(courseDetails.getName());
        System.out.println(courseDetails.getDescription());
    }

    public static void main(String[] args) {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(Wss4jClientConfiguration.class);

//        ApplicationContext context =
//                new AnnotationConfigApplicationContext(XwsClientConfiguration.class);
        CourseClient client = context.getBean(CourseClient.class);


        GetCourseDetailsResponse response = client.getCourseDetails(1);
        client.printResponse(response);

    }
}
