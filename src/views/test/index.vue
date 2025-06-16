<template>
  <div class="app-container">
    <el-form ref="formRef" :model="formData" :rules="rules" size="default" label-width="100px">
      <el-row gutter="15">
        <el-col :span="12">
          <el-form-item label="老人姓名" prop="name">
            <el-input v-model="formData.name" type="text" placeholder="请输入老人姓名" :maxlength="20"
              show-word-limit clearable :style="{width: '100%'}"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="身份证号" prop="idCardNo">
            <el-input v-model="formData.idCardNo" type="text" placeholder="请输入身份证号" clearable
              :style="{width: '100%'}"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row gutter="15">
        <el-col :span="12">
          <el-form-item label="出生日期" prop="birthday">
            <el-date-picker v-model="formData.birthday" format="YYYY-MM-DD" value-format="YYYY-MM-DD"
              :style="{width: '100%'}" placeholder="请选择出生日期" clearable></el-date-picker>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年龄" prop="age">
            <el-input v-model="formData.age" type="text" placeholder="请输入年龄" clearable
              :style="{width: '100%'}"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row gutter="15">
        <el-col :span="12">
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="formData.sex" size="default">
              <el-radio v-for="(item, index) in sexOptions" :key="index" :value="item.value"
                :disabled="item.disabled">{{item.label}}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系方式" prop="phone">
            <el-input v-model="formData.phone" type="text" placeholder="请输入联系方式" clearable
              :style="{width: '100%'}"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row gutter="15">
        <el-col :span="12">
          <el-form-item label="家庭住址" prop="address">
            <el-input v-model="formData.address" type="textarea" placeholder="请输入家庭住址"
              :autosize="{minRows: 4, maxRows: 4}" :style="{width: '100%'}"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="一寸照片" prop="image">
            <el-upload ref="image" :file-list="imagefileList" :action="imageAction"
              :before-upload="imageBeforeUpload">
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button type="primary" @click="submitForm">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script setup>
const {
  proxy
} = getCurrentInstance()
const formRef = ref()
const data = reactive({
  formData: {
    name: '',
    idCardNo: undefined,
    birthday: null,
    age: undefined,
    sex: 0,
    phone: undefined,
    address: undefined,
    image: null,
  },
  rules: {
    name: [{
      required: true,
      message: '请输入老人姓名',
      trigger: 'blur'
    }],
    idCardNo: [],
    birthday: [],
    age: [],
    sex: [],
    phone: [],
    address: [],
  }
})
const {
  formData,
  rules
} = toRefs(data)
const sexOptions = ref([{
  "label": "男",
  "value": 1
}, {
  "label": "女",
  "value": 0
}])
// 上传请求路径
const imageAction = ref('https://jsonplaceholder.typicode.com/posts/')
// 上传文件列表
const imagefileList = ref([])
/**
 * @name: 上传之前的文件判断
 * @description: 上传之前的文件判断，判断文件大小文件类型等
 * @param {*} file
 * @return {*}
 */
function imageBeforeUpload(file) {
  let isRightSize = file.size / 1024 / 1024 < 2
  if (!isRightSize) {
    proxy.$modal.msgError('文件大小超过 2MB')
  }
  return isRightSize
}
/**
 * @name: 表单提交
 * @description: 表单提交方法
 * @return {*}
 */
function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    // TODO 提交表单
  })
}
/**
 * @name: 表单重置
 * @description: 表单重置方法
 * @return {*}
 */
function resetForm() {
  formRef.value.resetFields()
}

</script>
<style>
.el-upload__tip {
  line-height: 1.2;
}

</style>
