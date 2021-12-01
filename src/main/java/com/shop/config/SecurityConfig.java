package com.shop.config;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity //SpringSecurityFilterChain이 자동으로 포함
public class SecurityConfig extends WebSecurityConfigurerAdapter { //오버라이딩을 통한 보안 설정 커스터마이징

    @Autowired
    MemberService memberService;

    @Override
    protected void configure(HttpSecurity http) throws Exception { //페이징 권한, 로그인 페이지, 로그아웃 메소드 등 설정 작성
        http.formLogin()
                .loginPage("/members/login") //로그인 페이지 URL 설정
                .defaultSuccessUrl("/")      //로그인 성공 시 이동할 URL 설정
                .usernameParameter("email")  //로그인 시 사용할 파라미터 이름 설정
                .failureUrl("/members/login/error")  //로그인 실패 시 이동할 URL 설정
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout")) //로그아웃 URL 설정
                .logoutSuccessUrl("/"); //로그아웃 성공 시 이동할 URL 설정

        http.authorizeRequests()    //시큐리티 처리에 HttpServletRequest를 이용한다는 의미
                .mvcMatchers("/", "/members/**",
                                "/item/**", "/images/**").permitAll()   //모든 사용자가 인증(로그인)없이 해당 경로에 접근
                .mvcMatchers("/admin/**").hasRole("ADMIN")     //해당 계정이 ADMIN Role일 경우에만 접근 가능
                .anyRequest().authenticated();    //나머지 경로들은 모두 인증을 요구하도록 설정

        http.exceptionHandling()
                .authenticationEntryPoint(new CustomAuthenicationEntryPoint()); //인증되지 않은 사용자가 리소스에 접근하였을 때 수행되는 핸들러 등록
    }
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**"); //static 디렉터리 하위 파일은 인증을 무시하도록 설정
    }


    @Bean
    public PasswordEncoder passwordEncoder(){ //비밀번호를 암호화하여 저장
        return new BCryptPasswordEncoder();
    }



    /*
    스프링 스큐리티에서 인증은 AuthenticationManager를 통해 이루어지며
    AuthenticationManagerBuilder가 AuthenticationManager를 생성
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        //userDetailsService를 구현하고 있는 객체로 memberService를 지정해주며, 비밀번호 암호화를 위해 passwordEncoder 지정
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
    }


}
