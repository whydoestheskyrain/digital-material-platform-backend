package com.trade.virtual_trade.controller;

import com.trade.virtual_trade.common.Result;
import com.trade.virtual_trade.entity.Product;
import com.trade.virtual_trade.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    // 1. 把 @Autowired 放到最上面（必须改）
    @Autowired
    private ProductService productService;

    // 管理员审核商品
    @PutMapping("/audit/{id}/{status}")
    public Result audit(
            @PathVariable Integer id,
            @PathVariable Integer status
    ) {
        productService.audit(id, status);
        return Result.success();
    }

    // 根据状态查询商品
    @GetMapping("/listByStatus/{status}")
    public Result listByStatus(@PathVariable Integer status) {
        return Result.success(productService.listByStatus(status));
    }

    // 查询我的商品
    @GetMapping("/myList/{sellerId}")
    public Result myList(@PathVariable Integer sellerId) {
        return Result.success(productService.myList(sellerId));
    }

    // 全部列表
    @GetMapping("/list")
    public Result<List<Product>> list() {
        return Result.success(productService.findAll());
    }

    // 分页
    @GetMapping("/page")
    public Result<List<Product>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(productService.findPage(page, size));
    }

    // 根据ID查询
    @GetMapping("/get/{id}")
    public Result<Product> get(@PathVariable Integer id) {
        return Result.success(productService.findById(id));
    }

    // 商品详情查询（公开接口）
    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Integer id) {
        return Result.success(productService.findById(id));
    }

    // 新增
    @PostMapping("/add")
    public Result add(@RequestBody Product product) {
        // 1. 如果前端没有传图片，设置默认图片
        // 后端 ProductController 里
        if (product.getImageUrl() == null || product.getImageUrl().trim().isEmpty()) {
            // 前端项目运行在 8082 端口，直接用前端路径即可
            product.setImageUrl("http://localhost:8082/images/game-default.png");
        }
        // 2. 设置初始状态（待审核）
        product.setStatus(0);

        // 3. 设置创建时间（使用 LocalDateTime.now()）
        product.setCreateTime(LocalDateTime.now());

        // 4. 保存商品
        productService.add(product);

        // 5. 返回成功
        return Result.success();
    }

    // 修改
    @PostMapping("/update")
    public Result update(@RequestBody Product product) {
        productService.update(product);
        return Result.success();
    }

    // 2. 删除必须用 @DeleteMapping（规范且安全）
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        productService.delete(id);
        return Result.success();
    }
}