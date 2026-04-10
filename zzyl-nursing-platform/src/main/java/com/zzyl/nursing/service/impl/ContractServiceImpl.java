package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.nursing.domain.Contract;
import com.zzyl.nursing.mapper.ContractMapper;
import com.zzyl.nursing.service.IContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 合同Service业务层处理
 * 
 * @author alexis
 * @date 2026-03-31
 */
@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements IContractService
{
    @Autowired
    private ContractMapper contractMapper;

    /**
     * 查询合同
     * 
     * @param id 合同主键
     * @return 合同
     */
    @Override
    public Contract selectContractById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询合同列表
     * 
     * @param contract 合同
     * @return 合同
     */
    @Override
    public List<Contract> selectContractList(Contract contract)
    {
        return contractMapper.selectContractList(contract);
    }

    /**
     * 新增合同
     * 
     * @param contract 合同
     * @return 结果
     */
    @Override
    public int insertContract(Contract contract)
    {
        return save(contract) ? 1 : 0;
    }

    /**
     * 修改合同
     * 
     * @param contract 合同
     * @return 结果
     */
    @Override
    public int updateContract(Contract contract)
    {
        return updateById(contract) ? 1 : 0;
    }

    /**
     * 批量删除合同
     * 
     * @param ids 需要删除的合同主键
     * @return 结果
     */
    @Override
    public int deleteContractByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除合同信息
     * 
     * @param id 合同主键
     * @return 结果
     */
    @Override
    public int deleteContractById(Long id)
    {
        return removeById(id) ? 1 : 0;
    }

    /**
     * 更新合同状态
     * 根据合同的开始时间和结束时间，自动更新合同状态：
     * - 0: 未开始（当前时间在开始时间之前）
     * - 1: 进行中（当前时间在开始时间和结束时间之间）
     * - 2: 已过期（当前时间在结束时间之后）
     * - 3: 已终止（不处理此状态）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContractStatus()
    {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        
        // 查询所有状态不为"已终止" (3) 的合同
        List<Contract> contracts = list(Wrappers.<Contract>lambdaQuery()
                .ne(Contract::getStatus, 3));
        
        // 如果没有需要处理的合同，直接返回
        if (contracts.isEmpty())
        {
            return;
        }

        // 存储需要更新的合同列表
        List<Contract> needUpdateContracts = new ArrayList<>();
        
        // 遍历所有合同，计算并更新状态
        for (Contract contract : contracts)
        {
            // 根据当前时间计算合同的最新状态
            Integer latestStatus = calculateContractStatus(contract, now);
            
            // 如果状态没有变化，跳过该合同
            if (Objects.equals(contract.getStatus(), latestStatus))
            {
                continue;
            }
            
            // 设置新的状态和更新时间
            contract.setStatus(latestStatus);
            contract.setUpdateTime(DateUtils.getNowDate());
            
            // 添加到待更新列表
            needUpdateContracts.add(contract);
        }

        // 如果有需要更新的合同，批量更新到数据库
        if (!needUpdateContracts.isEmpty())
        {
            updateBatchById(needUpdateContracts);
        }
    }

    /**
     * 计算合同状态
     * 
     * @param contract 合同对象
     * @param now 当前时间
     * @return 合同状态：0-未开始，1-进行中，2-已过期
     */
    private Integer calculateContractStatus(Contract contract, LocalDateTime now)
    {
        // 如果结束时间不为空且早于当前时间，返回"已过期" (2)
        if (contract.getEndDate() != null && contract.getEndDate().isBefore(now))
        {
            return 2;
        }
        
        // 如果开始时间不为空且晚于当前时间，返回"未开始" (0)
        if (contract.getStartDate() != null && contract.getStartDate().isAfter(now))
        {
            return 0;
        }
        
        // 其他情况（在开始时间和结束时间之间），返回"进行中" (1)
        return 1;
    }
}
