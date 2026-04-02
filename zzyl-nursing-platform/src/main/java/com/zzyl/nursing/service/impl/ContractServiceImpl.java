package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.nursing.domain.Contract;
import com.zzyl.nursing.mapper.ContractMapper;
import com.zzyl.nursing.service.IContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * Update contract status on schedule.
     */
    @Override
    public void updateContractStatus()
    {
        LocalDateTime now = LocalDateTime.now();
        List<Contract> contracts = list(Wrappers.<Contract>lambdaQuery()
                .ne(Contract::getStatus, 3));
        if (contracts.isEmpty())
        {
            return;
        }

        List<Contract> needUpdateContracts = new ArrayList<>();
        for (Contract contract : contracts)
        {
            Integer latestStatus = calculateContractStatus(contract, now);
            if (Objects.equals(contract.getStatus(), latestStatus))
            {
                continue;
            }
            contract.setStatus(latestStatus);
            contract.setUpdateTime(DateUtils.getNowDate());
            needUpdateContracts.add(contract);
        }

        if (!needUpdateContracts.isEmpty())
        {
            updateBatchById(needUpdateContracts);
        }
    }

    private Integer calculateContractStatus(Contract contract, LocalDateTime now)
    {
        if (contract.getEndDate() != null && contract.getEndDate().isBefore(now))
        {
            return 2;
        }
        if (contract.getStartDate() != null && contract.getStartDate().isAfter(now))
        {
            return 0;
        }
        return 1;
    }
}
