export const MAX_UPLOAD_SIZE = 20 * 1024 * 1024

const ALLOWED_DOCUMENT_TYPES = {
  pdf: {
    label: 'PDF',
    mimeTypes: ['application/pdf'],
    signatures: [[0x25, 0x50, 0x44, 0x46]],
  },
  docx: {
    label: 'DOCX',
    mimeTypes: [
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/zip',
    ],
    signatures: [[0x50, 0x4b, 0x03, 0x04], [0x50, 0x4b, 0x05, 0x06], [0x50, 0x4b, 0x07, 0x08]],
  },
}

export function getFileExtension(fileName = '') {
  return fileName.split('.').pop()?.toLowerCase() ?? ''
}

export async function validateThuyetMinhFile(file) {
  if (!file) return { valid: false, message: 'Vui lòng chọn file thuyết minh.' }
  if (file.size <= 0) return { valid: false, message: 'Tệp tin tải lên rỗng.' }
  if (file.size > MAX_UPLOAD_SIZE) return { valid: false, message: 'Dung lượng tối đa là 20MB cho mỗi file.' }

  const ext = getFileExtension(file.name)
  const rule = ALLOWED_DOCUMENT_TYPES[ext]
  if (!rule) return { valid: false, message: 'Chỉ hỗ trợ file PDF hoặc DOCX cho bản thuyết minh.' }

  if (file.type && !rule.mimeTypes.includes(file.type)) {
    return { valid: false, message: `File ${rule.label} có MIME type không hợp lệ.` }
  }

  const header = new Uint8Array(await file.slice(0, 8).arrayBuffer())
  const hasValidSignature = rule.signatures.some(signature =>
    signature.every((byte, index) => header[index] === byte)
  )

  if (!hasValidSignature) {
    return { valid: false, message: 'Tệp tin bị sai chữ ký định dạng thực tế. Vui lòng chọn đúng file PDF hoặc DOCX.' }
  }

  return { valid: true }
}
