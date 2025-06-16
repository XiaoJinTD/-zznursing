<template>
  <h1>这是子组件中的内容</h1>
  <div>{{ message }}</div>
  <button @click="sendMessage">给父组件发消息</button>
</template>

<script setup>
defineProps({
  message: String,
});

const emit = defineEmits(['message-sent']);
function sendMessage() {
  emit('message-sent', '来自子组件的消息');
}
</script>