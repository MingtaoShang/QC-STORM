/*
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU LESSER GENERAL PUBLIC LICENSE as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU LESSER GENERAL PUBLIC LICENSE for more details.

You should have received a copy of the GNU LESSER GENERAL PUBLIC LICENSE
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

#include "stdafx.h"

#include "SuperResolutionImageDisplay.h"




// super resolution image
CImg<unsigned char> CImg_SRImage(SR_IMAGE_DISPLAY_WIDTH, SR_IMAGE_DISPLAY_HIGH, 1, 3, 0);

CImgDisplay CImgDisp_SRImage;


