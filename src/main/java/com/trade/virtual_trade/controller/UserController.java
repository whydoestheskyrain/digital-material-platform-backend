package com.trade.virtual_trade.controller;

import com.trade.virtual_trade.common.Result;
import com.trade.virtual_trade.entity.User;
import com.trade.virtual_trade.mapper.UserMapper;
import com.trade.virtual_trade.service.UserService;
import com.trade.virtual_trade.service.TransactionLogService;
import com.trade.virtual_trade.utils.FileUploadUtils;
import com.trade.virtual_trade.utils.JwtUtil;
import com.trade.virtual_trade.utils.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    // 修改手机号接口
    @PutMapping("/update/phone")
    public Result updatePhone(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> params
    ) {
        Integer userId = JwtUtil.getUserId(token.substring(7));
        String newPhone = params.get("phone");
        try {
            boolean ok = userService.updatePhone(userId, newPhone);
            return Result.success("手机号修改成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }


    @Autowired
    private TransactionLogService transactionLogService;

    @GetMapping("/balance")
    public Result getUserBalance(@RequestHeader("Authorization") String token) {
        Integer userId = JwtUtil.getUserId(token.substring(7));
        User user = userMapper.selectBalanceById(userId);
        Map<String, Double> data = new HashMap<>();
        data.put("balance", user.getBalance().doubleValue());
        data.put("frozen", user.getFrozenBalance().doubleValue());
        return Result.success(data);
    }

    @GetMapping("/transactions")
    public Result getTransactions(@RequestHeader("Authorization") String token) {
        Integer userId = JwtUtil.getUserId(token.substring(7));
        return Result.success(transactionLogService.getUserLogs(userId));
    }

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 注册（密码加密存储）
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        User exist = userMapper.findByUsername(user.getUsername());
        if (exist != null) {
            return Result.error("用户名已存在");
        }
        // 密码加密存储
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.add(user);
        return Result.success("注册成功");
    }

    // 登录（生成 token，兼容明文密码自动升级为 BCrypt）
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User loginUser = userMapper.findByUsername(user.getUsername());
        if (loginUser == null) {
            return Result.error("账号或密码错误");
        }

        String storedPassword = loginUser.getPassword();
        boolean passwordMatch = false;

        // 1. 先尝试用 BCrypt 验证（新用户/已升级）
        if (storedPassword != null && storedPassword.startsWith("$2")) {
            passwordMatch = passwordEncoder.matches(user.getPassword(), storedPassword);
        }
        // 2. 兼容旧明文密码（未升级）
        else if (storedPassword != null && storedPassword.equals(user.getPassword())) {
            passwordMatch = true;
            // 自动升级为 BCrypt 加密
            loginUser.setPassword(passwordEncoder.encode(user.getPassword()));
            userMapper.update(loginUser);
        }

        if (!passwordMatch) {
            return Result.error("账号或密码错误");
        }

        String token = JwtUtil.generateToken(loginUser.getId());
        loginUser.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", loginUser);
        data.put("token", token);
        return Result.success(data);
    }

    // 上传头像（从 Authorization 头解析 token）

    @PostMapping("/upload/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file,
                               @RequestHeader(value = "Authorization", required = false) String authorization) {
        System.out.println("Authorization header: " + authorization);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            System.out.println("Extracted token: " + token);
            Integer userId = JwtUtil.getUserId(token);
            System.out.println("Parsed userId: " + userId);
        }
        try {
            // 1. 校验 token 存在
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return Result.error("未登录或 token 无效");
            }
            String token = authorization.substring(7); // 去掉 "Bearer "
            Integer userId = JwtUtil.getUserId(token);
            if (userId == null) {
                return Result.error("token 已过期或无效");
            }

            // 2. 保存头像文件
            String avatarUrl = FileUploadUtils.upload(file, "avatar");

            // 3. 更新数据库
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            user.setAvatar(avatarUrl);
            userMapper.update(user);

            // 4. 返回图片 URL
            return Result.success(avatarUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }


    // 修改昵称
    @PutMapping("/update/nickname")
    public Result updateNickname(@RequestBody User user) {
        userService.updateNickname(user);
        return Result.success();
    }

    // 修改密码（使用 BCrypt 验证和加密）
    @PutMapping("/update/password")
    public Result updatePassword(@RequestBody Map<String, String> map) {
        Integer id = Integer.valueOf(map.get("id"));
        String oldPassword = map.get("oldPassword");
        String newPassword = map.get("newPassword");
        
        // 查询用户获取加密后的密码
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("旧密码错误");
        }
        
        // 加密新密码并更新
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.update(user);
        return Result.success("密码修改成功");
    }

    @GetMapping("/list")
    public Result<List<User>> list() {
        return Result.success(userService.findAll());
    }

    // ======================
    // 👇 就是少了这个！！！
    // ======================
    @GetMapping("/page")
    public Result<List<User>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return Result.success(userService.findPage(page, size));
    }

    @GetMapping("/get/{id}")
    public Result<User> get(@PathVariable Integer id) {
        return Result.success(userService.findById(id));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }

    @GetMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        userService.delete(id);
        return Result.success();
    }
    @GetMapping("/info")
    public Result getUserInfo(@RequestHeader("Authorization") String authorization) {
        // 1. 提取 token
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error("未登录");
        }
        String token = authorization.substring(7);
        Integer userId = JwtUtil.getUserId(token);
        if (userId == null) {
            return Result.error("token无效");
        }
        // 2. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null); // 清除密码
        return Result.success(user);
    }

}