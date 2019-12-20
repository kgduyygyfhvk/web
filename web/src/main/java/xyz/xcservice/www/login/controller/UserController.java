package xyz.xcservice.www.login.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import springfox.documentation.annotations.ApiIgnore;
import xyz.xcservice.www.base.ResultResponse;
import xyz.xcservice.www.dto.UserDetailDTO;
import xyz.xcservice.www.dto.UserLoginDetails;
import xyz.xcservice.www.enums.SystemResponseCodeEnum;
import xyz.xcservice.www.login.entity.UserAuthoritiesPO;
import xyz.xcservice.www.login.entity.UserLoginPO;
import xyz.xcservice.www.login.service.UserAuthoritiesService;
import xyz.xcservice.www.login.service.UserLoginService;
import xyz.xcservice.www.utils.JwtTokenUtil;

import javax.annotation.Resource;
import java.util.Set;

/**
 * <p>
 * 用户登录表 前端控制器
 * </p>
 *
 * @author wuwenchao
 * @since 2019-12-20
 */
@RestController
@Slf4j
@Api(value = "loginManage", description = "账号管理API")
public class UserController {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Resource
    private UserAuthoritiesService userAuthoritiesService;

    @Resource
    private UserLoginService userLoginService;


    @PostMapping("/login")
    @ResponseBody
    @ApiIgnore
    @ApiOperation(value = "login", notes = "login", response = ResultResponse.class)
    public ResultResponse<String> login(@RequestBody UserDetailDTO userDetailDTO) {
        log.info("开始验证登录账号:[{}]", userDetailDTO.getLoginCode());
        //开始验证账号密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDetailDTO.getLoginCode(), userDetailDTO.getPassword())
        );
        String token = JwtTokenUtil.createToken((String) authentication.getPrincipal(), (Set) authentication.getAuthorities());
        //生成token
        log.info("验证成功:[{}]", userDetailDTO.getLoginCode());
        return new ResultResponse<>(token);
    }

    @PostMapping("/register")
    @ResponseBody
    @ApiIgnore
    @ApiOperation(value = "register", notes = "register", response = ResultResponse.class)
    public ResultResponse<Boolean> register(@RequestBody UserDetailDTO userDetailDTO) {
        UserLoginPO userLoginPO = new UserLoginPO();
        userLoginPO.setLoginCode(userDetailDTO.getLoginCode());
        userLoginPO.setPassword(bCryptPasswordEncoder.encode(userDetailDTO.getPassword()));
        boolean isSuccess = userLoginService.save(userLoginPO);
        if (isSuccess) {
            log.info("[{}]用户登录表创建成功", userDetailDTO.getLoginCode());
        }

        //查询
        QueryWrapper<UserLoginPO> queryWrapper = new QueryWrapper();
        queryWrapper.eq("login_code", userLoginPO.getLoginCode());
        UserLoginPO userLoginPO1 = userLoginService.getOne(queryWrapper);
        UserAuthoritiesPO userAuthoritiesPO = new UserAuthoritiesPO();
        userAuthoritiesPO.setUserLoginId(userLoginPO1.getId());
        userAuthoritiesPO.setAuthority("USER");
        userAuthoritiesService.save(userAuthoritiesPO);
        return new ResultResponse<>(SystemResponseCodeEnum.SUCCESS);
    }
}




