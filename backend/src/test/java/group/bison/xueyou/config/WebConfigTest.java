   package group.bison.xueyou.config;

   import group.bison.xueyou.interceptor.AuthInterceptor;
   import org.junit.jupiter.api.BeforeEach;
   import org.junit.jupiter.api.Test;
   import org.mockito.ArgumentCaptor;
   import org.mockito.InjectMocks;
   import org.mockito.Mock;
   import org.mockito.MockitoAnnotations;
   import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
   import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

   import static org.junit.jupiter.api.Assertions.assertEquals;
   import static org.mockito.Mockito.*;

   public class WebConfigTest {

       @Mock
       private AuthInterceptor authInterceptor;

       @Mock
       private InterceptorRegistry registry;

       @Mock
       private InterceptorRegistration registration;

       @InjectMocks
       private WebConfig webConfig;

       @BeforeEach
       public void setUp() {
           MockitoAnnotations.openMocks(this);
       }

       @Test
       public void addInterceptors_IncludePattern_ShouldAddInterceptor() {
           when(registry.addInterceptor(authInterceptor)).thenReturn(registration);

           webConfig.addInterceptors(registry);

           verify(registry).addInterceptor(authInterceptor);
           ArgumentCaptor<String[]> patternsCaptor = ArgumentCaptor.forClass(String[].class);
           verify(registration).addPathPatterns(patternsCaptor.capture());
           assertEquals(1, patternsCaptor.getValue().length);
           assertEquals("/api/**", patternsCaptor.getValue()[0]);
       }

       @Test
       public void addInterceptors_ExcludePattern_ShouldNotAddInterceptor() {
           when(registry.addInterceptor(authInterceptor)).thenReturn(registration);

           webConfig.addInterceptors(registry);

           ArgumentCaptor<String[]> excludePatternsCaptor = ArgumentCaptor.forClass(String[].class);
           verify(registration).excludePathPatterns(excludePatternsCaptor.capture());
           assertEquals(3, excludePatternsCaptor.getValue().length);
           assertEquals("/api/sendCode", excludePatternsCaptor.getValue()[0]);
           assertEquals("/api/verify", excludePatternsCaptor.getValue()[1]);
           assertEquals("/api/fingerprint", excludePatternsCaptor.getValue()[2]);
       }
   }
   