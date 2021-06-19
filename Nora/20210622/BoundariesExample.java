/**
 * 설명 : ImageReaderSpi.class 라는 외부 코드를 경계하지 않아 발생한 사례
 *
 */

/**
 * Gets image dimensions for given file
 */
public static Integer getImageDimensions(Object input,String imgUrl)throws IOException{

        try(ImageInputStream stream=ImageIO.createImageInputStream(input)){ // accepts File, InputStream, RandomAccessFile
            if(stream!=null){
                // IIORegistry : 정의된 서비스의 로드를 경량화 한다.
                IIORegistry iioRegistry=IIORegistry.getDefaultInstance();
                //ImageReaderSpi 는 연결된 ImageReader 클래스 에 대한 여러 유형의 정보를 제공합니다.
                Iterator<ImageReaderSpi> iter=iioRegistry.getServiceProviders(ImageReaderSpi.class,true);
                while(iter.hasNext()){
                    ImageReaderSpi readerSpi=iter.next();
                    // canDecodeInput : BMP / GIF / JPEG / PNG / WBMP 확장자만 디코드 가능
                    // WEBP 확장자는 처리를 못하여 예외가 발생하고 있었다 > 학습 테스트 후, 반영을 했으면 그에 대한 조치를 했을 것이다.
                    if(readerSpi.canDecodeInput(stream)){
                        ImageReader reader=readerSpi.createReaderInstance();
                        try{
                            reader.setInput(stream);
                            int width=reader.getWidth(reader.getMinIndex());
                            int height=reader.getHeight(reader.getMinIndex());
                            return Math.max(width,height);
                        }finally{
                        reader.dispose();
                        }
                    }else{
                        // canDecodeInput에 대한 조치로 아래와 같이 처리.. 이도 개선할 필요가 있긴함
                        // 결론은 ImageReaderSpi.class 라는 외부 코드를 가져올 때 경계를 안해서 발생한 사례임! 경계하자~
                        try{
                            return getImageWidth(imgUrl);
                        }catch(Exception e){
                            throw new IllegalArgumentException("Can't find decoder for this image");
                        }
                    }
                }
                throw new IllegalArgumentException("Can't find decoder for this image");
                }else{
                throw new IllegalArgumentException("Can't open stream for this image");
                }
            }
        }