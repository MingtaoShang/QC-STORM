function grad=NumericalGradient(inInf, FitingData)


for i=1:length(inInf)
    if(inInf(i)<0)
        inInf(i)=0; %��Χ����
    end
end

nx=length(inInf);
grad=zeros(nx,1);

for i=1:nx
    %�����Ľ��Ʒ��������������ƫ��
	b1=inInf;
	b2=inInf;
    
	b1(i)=inInf(i)-0.001;
    L1=LossFunction(b1, FitingData);
    
	b2(i)=inInf(i)+0.001;
    L2=LossFunction(b2, FitingData);
    
    %�����õ���΢�֣�������㲻��׼ȷ���Ѿ�ʵ����֤
	
    if(inInf(i)<0)
       grad(i)=0; 
    else
        grad(i)=(L2-L1)/0.002;
    end
end
