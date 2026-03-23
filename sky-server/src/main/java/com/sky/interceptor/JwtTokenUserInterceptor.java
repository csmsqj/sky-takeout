package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    // 【深度剖析：为什么返回类型必须是 boolean？】
    // return true：保安开闸放行，请求继续走到后续的 Controller 执行业务逻辑。
    // return false：保安无情拉下闸门！请求在此地夭折，直接退回，Controller 根本不知道有人来过。
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 从请求头拿到前端带来的 token
        String token = request.getHeader(jwtProperties.getUserTokenName());

        try {
            // 2. 校验令牌（JWT 的第二阶段：验印）
            // 【深度扫盲1：第一个参数到底是什么？从哪得到的？】
            // 误区纠正：parseJWT 的第一个参数 jwtProperties.getUserSecretKey() 绝对不是什么“设置的用户名”！
            // 它是我们在前文 application.yml 里设置的全局通用【签名密钥】（比如 "itheimaCend"
            // 这个随机又复杂的字符串）。SpringBoot 启动时把它自动读取到了 JwtProperties
            // 配置类中，我们的工具类必须拿着这把仅存在于后端的“钥匙”才能验证真伪。

            // 【深度扫盲2：为什么要密钥？黑客随意篡改 Token 为什么会失败？】
            // 误区纠正：JWT 并不是加密的！它的中间部分（Payload）存着 userId，但这仅仅是简单的 Base64 编码，谁都能轻松解码查看。
            // 危险点：既然没加密，黑客可以非常轻易地解码，把 payload 里的 userId=5 改成 userId=1（老板的账号），然后再发给服务器。
            // 防伪核心：Token
            // 的尾部第三部分是“数字签名”。生成这串防伪签名时，必须把明文数据加上后端深藏的那把“密钥”一起进行极其复杂的不可逆哈希运算（HS256）。黑客因为不知道密钥，所以他篡改了
            // userId 后，绝对算不出匹配的新签名！
            // 当服务器拦截器执行 parseJWT 时，会用正确的密钥对着黑客发来的假数据重新算一次签名。比对之下发现新算出的签名和 Token
            // 尾部原本的签名根本对不上，立刻抛出 SignatureException 异常，让伪造的 Token 原形毕露！
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

            // 3. 提取 userId
            // 【深度扫盲3：Long.valueOf() 方法的作用是什么？】
            // claims.get(JwtClaimsConstant.USER_ID) 从 Token 载荷里抠出数据时，它的类型只是个最宽泛的
            // Object（万物之父），不能直接当做数字来用。
            // 步骤 1：我们先用 .toString() 把它变成普通的文本字符串（比如变成文本 "10086"）。
            // 步骤 2：Long.valueOf("10086") 登场！它的唯一核心作用就是：“数据类型的安全翻译与转换”。它负责把外表是字符串形态的
            // "10086"，精确强制转换为 Java 引擎真正认识的 64位长整型大数字对象 (Long类型 10086L)。
            // 因为数据库的 user 表主键 ID 是 bigint，对应的 Java 类型必须是 Long。只有转换成纯正的大数字类型，Java 拿着它存入
            // ThreadLocal、去数据库查库、做业务逻辑时，才不会报“类型不匹配”的系统错误！
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());

            // 4. 【进阶考点】：存入 ThreadLocal！
            // 请求从 拦截器 -> Controller -> Service 都在同一个执行线程里。
            // 存进 ThreadLocal（当前线程专属的私有储物柜），后续 Service 随时随地都可以拿出来用
            // (BaseContext.getCurrentId())，避免了在方法参数里传来传去的恶心代码。
            BaseContext.setCurrentId(userId);

            return true; // 校验合法，放行！

        } catch (Exception ex) {
            // 【异常实战闭环：这里为什么用 try-catch 拦截，而不用 throws？】
            // (详细解释见下方第七部分的异常大师课)
            response.setStatus(401);
            return false; // 拦截！
        }
    }
}
