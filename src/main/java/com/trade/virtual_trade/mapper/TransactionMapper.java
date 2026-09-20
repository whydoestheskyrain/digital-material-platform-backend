package com.trade.virtual_trade.mapper;
import com.trade.virtual_trade.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
/**
 * 交易流水Mapper层，处理transaction表的数据库操作
 */
@Mapper
public interface TransactionMapper {
    // 查询所有交易流水
    List<Transaction> selectAll();
    // 根据ID查询单条交易流水
    Transaction selectById(Integer id);
    // 新增交易流水
    int insert(Transaction transaction);
    // 修改交易流水信息
    int update(Transaction transaction);
    // 根据ID删除交易流水
    int deleteById(Integer id);
    List<Transaction> findPage(int offset, int size);
}