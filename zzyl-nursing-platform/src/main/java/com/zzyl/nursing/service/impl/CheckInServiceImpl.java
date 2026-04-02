package com.zzyl.nursing.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import cn.hutool.core.util.IdcardUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.common.exception.base.BaseException;
import com.zzyl.common.utils.CodeGenerator;
import com.zzyl.common.utils.StringUtils;
import com.zzyl.nursing.domain.Bed;
import com.zzyl.nursing.domain.CheckIn;
import com.zzyl.nursing.domain.CheckInConfig;
import com.zzyl.nursing.domain.Contract;
import com.zzyl.nursing.domain.Elder;
import com.zzyl.nursing.dto.CheckInApplyDto;
import com.zzyl.nursing.dto.CheckInElderDto;
import com.zzyl.nursing.mapper.BedMapper;
import com.zzyl.nursing.mapper.CheckInConfigMapper;
import com.zzyl.nursing.mapper.CheckInMapper;
import com.zzyl.nursing.mapper.ContractMapper;
import com.zzyl.nursing.mapper.ElderMapper;
import com.zzyl.nursing.service.ICheckInService;
import com.zzyl.nursing.vo.CheckInConfigVo;
import com.zzyl.nursing.vo.CheckInDetailVo;
import com.zzyl.nursing.vo.CheckInElderVo;
import com.zzyl.nursing.vo.ElderFamilyVo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 入住Service业务层处理
 *
 * @author alexis
 * @date 2026-03-31
 */
@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements ICheckInService
{
    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private ElderMapper elderMapper;

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private CheckInConfigMapper checkInConfigMapper;

    @Autowired
    private ContractMapper contractMapper;

    /**
     * 查询入住
     *
     * @param id 入住主键
     * @return 入住
     */
    @Override
    public CheckIn selectCheckInById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询入住列表
     *
     * @param checkIn 入住
     * @return 入住
     */
    @Override
    public List<CheckIn> selectCheckInList(CheckIn checkIn)
    {
        return checkInMapper.selectCheckInList(checkIn);
    }

    /**
     * 新增入住
     *
     * @param checkIn 入住
     * @return 结果
     */
    @Override
    public int insertCheckIn(CheckIn checkIn)
    {
        return save(checkIn) ? 1 : 0;
    }

    /**
     * 修改入住
     *
     * @param checkIn 入住
     * @return 结果
     */
    @Override
    public int updateCheckIn(CheckIn checkIn)
    {
        return updateById(checkIn) ? 1 : 0;
    }

    /**
     * 批量删除入住
     *
     * @param ids 需要删除的入住主键
     * @return 结果
     */
    @Override
    public int deleteCheckInByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除入住信息
     *
     * @param id 入住主键
     * @return 结果
     */
    @Override
    public int deleteCheckInById(Long id)
    {
        return removeById(id) ? 1 : 0;
    }

    /**
     * 申请入住
     *
     * @param checkInApplyDto 申请入住信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(CheckInApplyDto checkInApplyDto)
    {
        CheckInElderDto checkInElderDto = checkInApplyDto.getCheckInElderDto();
        if (checkInElderDto == null || checkInApplyDto.getCheckInConfigDto() == null || checkInApplyDto.getCheckInContractDto() == null)
        {
            throw new BaseException("申请入住参数不完整");
        }

        LambdaQueryWrapper<Elder> elderQueryWrapper = new LambdaQueryWrapper<>();
        elderQueryWrapper.eq(Elder::getIdCardNo, checkInElderDto.getIdCardNo());
        elderQueryWrapper.eq(Elder::getStatus, 1);
        Elder elder = elderMapper.selectOne(elderQueryWrapper);
        if (ObjectUtils.isNotEmpty(elder))
        {
            throw new BaseException("老人已入住");
        }

        Bed bed = bedMapper.selectById(checkInApplyDto.getCheckInConfigDto().getBedId());
        if (bed == null)
        {
            throw new BaseException("床位不存在");
        }
        if (!Integer.valueOf(0).equals(bed.getBedStatus()))
        {
            throw new BaseException("床位已被占用");
        }
        bed.setBedStatus(1);
        bedMapper.updateById(bed);

        elder = insertOrUpdate(bed, checkInElderDto);

        String contractNo = "HT" + CodeGenerator.generateContractNumber();
        insertContract(contractNo, elder, checkInApplyDto);

        CheckIn checkIn = insertCheckIn(elder, checkInApplyDto);
        insertCheckInConfig(checkIn.getId(), checkInApplyDto);
    }

    /**
     * 新增入住配置
     *
     * @param checkInId 入住ID
     * @param checkInApplyDto 申请入住信息
     */
    private void insertCheckInConfig(Long checkInId, CheckInApplyDto checkInApplyDto)
    {
        CheckInConfig checkInConfig = new CheckInConfig();
        BeanUtils.copyProperties(checkInApplyDto.getCheckInConfigDto(), checkInConfig);
        checkInConfig.setCheckInId(checkInId);
        checkInConfigMapper.insert(checkInConfig);
    }

    /**
     * 新增入住信息
     *
     * @param elder 老人
     * @param checkInApplyDto 申请入住信息
     * @return 入住记录
     */
    private CheckIn insertCheckIn(Elder elder, CheckInApplyDto checkInApplyDto)
    {
        CheckIn checkIn = new CheckIn();
        checkIn.setElderId(elder.getId());
        checkIn.setElderName(elder.getName());
        checkIn.setIdCardNo(elder.getIdCardNo());
        checkIn.setNursingLevelName(checkInApplyDto.getCheckInConfigDto().getNursingLevelName());
        checkIn.setStartDate(checkInApplyDto.getCheckInConfigDto().getStartDate());
        checkIn.setEndDate(checkInApplyDto.getCheckInConfigDto().getEndDate());
        checkIn.setBedNumber(elder.getBedNumber());
        checkIn.setRemark(JSON.toJSONString(checkInApplyDto.getElderFamilyDtoList()));
        checkIn.setStatus(0);
        checkInMapper.insert(checkIn);
        return checkIn;
    }

    /**
     * 新增合同
     *
     * @param contractNo 合同编号
     * @param elder 老人
     * @param checkInApplyDto 申请入住信息
     */
    private void insertContract(String contractNo, Elder elder, CheckInApplyDto checkInApplyDto)
    {
        if (StringUtils.isBlank(checkInApplyDto.getCheckInContractDto().getAgreementPath()))
        {
            throw new BaseException("请上传合同文件");
        }

        Contract contract = new Contract();
        BeanUtils.copyProperties(checkInApplyDto.getCheckInContractDto(), contract);
        contract.setContractNumber(contractNo);
        contract.setElderId(Math.toIntExact(elder.getId()));
        contract.setElderName(elder.getName());

        LocalDateTime checkInStartTime = checkInApplyDto.getCheckInConfigDto().getStartDate();
        LocalDateTime checkInEndTime = checkInApplyDto.getCheckInConfigDto().getEndDate();
        Integer status = checkInStartTime != null && checkInStartTime.isAfter(LocalDateTime.now()) ? 0 : 1;
        contract.setStatus(status);
        contract.setStartDate(checkInStartTime);
        contract.setEndDate(checkInEndTime);
        contractMapper.insert(contract);
    }

    /**
     * 新增或更新老人
     *
     * @param bed 床位
     * @param checkInElderDto 老人信息
     * @return 老人
     */
    private Elder insertOrUpdate(Bed bed, CheckInElderDto checkInElderDto)
    {
        Elder elder = new Elder();
        BeanUtils.copyProperties(checkInElderDto, elder);
        elder.setBedNumber(bed.getBedNumber());
        elder.setBedId(bed.getId());
        elder.setStatus(1);

        LambdaQueryWrapper<Elder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Elder::getIdCardNo, checkInElderDto.getIdCardNo()).ne(Elder::getStatus, 1);
        Elder elderDb = elderMapper.selectOne(lambdaQueryWrapper);
        if (ObjectUtils.isNotEmpty(elderDb))
        {
            elder.setId(elderDb.getId());
            elderMapper.updateById(elder);
            return elder;
        }

        elderMapper.insert(elder);
        return elder;
    }

    /**
     * 查询入住详情
     *
     * @param id 入住id
     * @return 入住详情
     */
    @Override
    public CheckInDetailVo detail(Long id)
    {
        CheckInDetailVo checkInDetailVo = new CheckInDetailVo();

        CheckInConfigVo checkInConfigVo = new CheckInConfigVo();
        CheckIn checkIn = checkInMapper.selectById(id);
        BeanUtils.copyProperties(checkIn, checkInConfigVo);

        CheckInConfig checkInConfig = checkInConfigMapper.selectOne(new LambdaQueryWrapper<CheckInConfig>()
                .eq(CheckInConfig::getCheckInId, id));
        BeanUtils.copyProperties(checkInConfig, checkInConfigVo);
        checkInDetailVo.setCheckInConfigVo(checkInConfigVo);

        CheckInElderVo checkInElderVo = new CheckInElderVo();
        Long elderId = checkIn.getElderId();
        Elder elder = elderMapper.selectById(elderId);
        BeanUtils.copyProperties(elder, checkInElderVo);
        checkInElderVo.setAge(IdcardUtil.getAgeByIdCard(elder.getIdCardNo()));
        checkInDetailVo.setCheckInElderVo(checkInElderVo);

        String remark = checkIn.getRemark();
        List<ElderFamilyVo> elderFamilyVos = JSON.parseArray(remark, ElderFamilyVo.class);
        checkInDetailVo.setElderFamilyVoList(elderFamilyVos);

        Contract contract = contractMapper.selectOne(new LambdaQueryWrapper<Contract>()
                .eq(Contract::getElderId, Math.toIntExact(elderId)));
        checkInDetailVo.setContract(contract);

        return checkInDetailVo;
    }
}
